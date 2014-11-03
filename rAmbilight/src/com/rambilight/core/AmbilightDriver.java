package com.rambilight.core;

import com.legge.preferences.lMath;
import com.legge.preferences.Preferences;
import com.rambilight.core.serial.ComDriver;
import com.rambilight.core.ui.MessageBox;
import com.rambilight.core.ui.TrayController;
import com.rambilight.plugins.Ambilight.Ambilight;
import com.rambilight.plugins.Built_In_Effects.Built_In_Effects;
import com.rambilight.plugins.PushBullet.PushBullet;

import javax.swing.*;

public class AmbilightDriver {

    private static TrayController tray;
    private static ComDriver      serialCom;

    /**
     * Start the application
     *
     * @param args The input from the command line
     */
    public static void main(String[] args) throws Exception {

        // Arguments: String, invert?, switch every other?
        System.out.println(invert("This is the recursive function that was needed in the program. The sinus of 45 is " + lMath.sin(lMath.toRad(45)) + " and the tangent of -60 is " + lMath.tan(lMath.toRad(-60)) + ". These values are calculated using power series.", true, true));

        // Set the UI to a theme that resembles the platform specific one.
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Unable to set the look and feel.");
        }

        // Safer way to do things...
        try {
            Preferences.setPathBySystem("rAmbilight", "rAmbilight.conf");
            Preferences.read();
            Global.loadPreferences();
            serialCom = new ComDriver();

            //ModuleLoader.loadModules(ModuleLoader.loadExternalModules(AmbilightDriver.class));
            ModuleLoader.loadModule(Ambilight.class);
            ModuleLoader.loadModule(PushBullet.class);
            ModuleLoader.loadModule(Built_In_Effects.class);

            tray = new TrayController();

            for (String moduleName : Global.currentControllers)
                ModuleLoader.activateModule(moduleName);

            serialCom.initialize();
        } catch (Exception e) {
            e.printStackTrace();
            MessageBox.Error(e.getMessage()); // Displays an error box in case of something happens
            exit(-1);
            return;
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
     * Global function for getting the serial communication device
     *
     * @return Returns the currently active Serial communications device
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
     * @code Error code, 0 for safe exit
     */
    private static void exit(int code) {
        try {
            ModuleLoader.dispose();
            Global.currentControllers = ModuleLoader.getActiveModules().toArray(new String[ModuleLoader.getActiveModules().size()]);
            Global.savePreferences();

            Preferences.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            tray.remove();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            serialCom.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (code == 0)
            System.out.println("Exiting");
        else
            System.out.println("Exiting with error code " + code);
        System.exit(code);
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
                            System.err.println("An error occurred in the main thread.");
                            e.printStackTrace();
                        }
                    }
                    else {
                        if (!suspended) {
                            suspended = true;
                            ModuleLoader.suspend();
                        }
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            System.out.println("Thread sleep was interrupted.");
                        }
                    }
                } catch (Exception e) {
                    MessageBox.Error(e.getMessage()); // Displays an error box in case of something happens
                    e.printStackTrace();
                }
            exit(0);
        }
    }
}