package com.rambilight.core;

import com.rambilight.core.preferences.Global;
import com.rambilight.core.preferences.Preferences;
import com.rambilight.core.serial.ComDriver;
import com.rambilight.core.ui.MessageBox;
import com.rambilight.core.ui.TrayController;

import javax.swing.*;

public class AmbilightDriver {

    private static TrayController tray;
    private static ComDriver serialCom;

    /**
     * Start the application
     *
     * @args The input from for example the commandline
     */
    public static void main(String[] args) throws Exception {

        // Set the UI to a theme that resembles the platform specific one.
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
        }

        // Safer way to do things...
        try {

            Preferences.read();
            Global.loadPreferences();

            serialCom = new ComDriver();

            System.out.println(invert("This is the recursive function that was needed in the program", true, true));

            ModuleLoader.loadModule(com.rambilight.plugins.Ambilight.Ambilight.class);

            //ModuleLoader.loadModules(ModuleLoader.loadExternalModules(AmbilightDriver.class));

            tray = new TrayController();

            for (String moduleName : Global.currentControllers)
                ModuleLoader.activateModule(moduleName);

            serialCom.initialize();
        } catch (Exception e) {
            e.getStackTrace();
            MessageBox.Error(e.getMessage()); // Displays an error box in case of something happens
            exit(-1);
        }
        new Thread(new Runtime()).start();
    }

    /**
     * A recursive function...
     *
     * @param str     Original string
     * @param first   Use the original first vale as the output first value
     * @param weirdly Do the invert in a weird way
     * @return A kind of inverted string
     */
    public static String invert(String str, boolean first, boolean weirdly) {
        String[] strs = str.split(" ", 2);
        if (strs.length > 1) {
            if (weirdly)
                first = !first;
            String theRest = invert(strs[1], first, weirdly);
            if (first)
                strs[0] = theRest + " " + strs[0];
            else
                strs[0] += " " + theRest;
        }
        return strs[0];
    }

    /**
     * Global function for getting the seril comunication device
     *
     * @return Returns the currently active Serial comunications device
     */
    public static ComDriver getSerialCom() {
        return serialCom;
    }

    /**
     * Global function for exiting the application under controlled manners
     */
    public static void requestExit() {
        Global.requestExit = true;
    }

    /**
     * Private function for exiting the application and releasing all assets
     *
     * @code Errorcode, 0 for safe exit
     */
    private static void exit(int code) {
        try {
            ModuleLoader.dispose();

            Global.currentControllers = ModuleLoader.getActiveModules().toArray(new String[ModuleLoader.getActiveModules().size()]);
            Global.savePreferences();
            Preferences.flush();
            tray.remove();
            serialCom.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (code == 0)
            System.out.println("Exiting");
        else
            System.out.println("Exiting with errorcode " + code);
        System.exit(0);
    }

    /**
     * The infinite loop the application depends on that runs in the background
     */
    private static class Runtime implements Runnable {

        boolean suspended;

        public void run() {
            while (!Global.requestExit)
                try {
                    if (Global.isActive) {
                        if (suspended)
                            suspended = false;
                        ModuleLoader.step();
                        serialCom.update();
                        try {
                            Thread.sleep(10); // sleep for a while, to keep the CPU usage down.
                        } catch (InterruptedException e) {
                        }
                    } else {
                        if (!suspended) {
                            suspended = true;
                            ModuleLoader.suspend();
                        }
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                        }
                    }
                } catch (Exception e) {
                    MessageBox.Error(e.getMessage()); // Displays an errorbox in case of someting happends
                    e.printStackTrace();
                }
            exit(0);
        }
    }
}