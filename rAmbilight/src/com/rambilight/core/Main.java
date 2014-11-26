package com.rambilight.core;

import com.legge.lMath;
import com.legge.preferences.Preferences;
import com.rambilight.core.api.Global;
import com.rambilight.core.api.ui.MessageBox;
import com.rambilight.core.api.ui.TrayController;
import com.rambilight.core.serial.ComDriver;

import javax.swing.*;
import java.util.concurrent.CountDownLatch;

public class Main {

    private static TrayController tray;
    private static ComDriver      serialCom;

    public static CountDownLatch sleepLatch;

    public static void trayControllerSetMessage(String message, boolean icon) {
        tray.setLabel(message, icon);
    }

    /**
     * Start the application
     *
     * @param args The input from the command line
     */
    public static void main(String[] args) throws Exception {

        // Arguments: String, invert?, switch every other?
        System.out.println(invert("This is the recursive function that was needed in the program. The sinus of 45 is " + lMath.rsin(lMath.toRad(45)) + " and the tangent of -60 is " + lMath.tan(lMath.toRad(-60)) + ". These values are calculated using power series.", true, true));

        System.setProperty("apple.laf.useScreenMenuBar", "false");
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "rAmbilight");
        // Set the UI to a theme that resembles the platform specific one.
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Unable to set the look and feel.");
        }

        // Safer way to do things...
        try {
            Global.generateApplicationSupportPath();
            Preferences.setPathToDefault();
            Preferences.read();

            Global.loadPreferences();
            serialCom = new ComDriver();

            ModuleLoader.loadModules(ModuleLoader.loadExternalModules(Main.class));
            //ModuleLoader.loadModule(Ambilight.class);

            tray = new TrayController();

            for (String moduleName : Global.currentControllers)
                ModuleLoader.activateModule(moduleName);
            tray.enableRun();
        } catch (Exception e) {
            e.printStackTrace();
            String message = e.getMessage();
            Throwable cause = e.getCause();
            String error = "";

            if (message != null)
                error += message;
            if (cause != null)
                error += "\nCaused by: " + cause.getMessage();

            MessageBox.Error(message != null ? message : "Initialization error!", error + "\nShutting down..."); // Displays an error box in case of something happens
            exit(-1);
            return;
        }
        Thread thread = new Thread(new Runtime());
        thread.setName("rAmbilight Runtime");
        thread.run();
    }

    /**
     * A recursive function...
     *
     * @param str     Original string
     * @param first   Use the original first vale as the output first value
     * @param weirdly Do the invert in a weird way
     * @return A kind of inverted string
     */
    private static String invert(String str, boolean first, boolean weirdly) {
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
     * The infinite loop the application depends on that runs in the background
     */
    private static class Runtime implements Runnable {

        boolean suspended;

        public void run() {
            while (true)
                if (!Global.requestExit)
                    try {
                        if (Global.isSerialConnectionActive && Global.isActive) {
                            if (suspended)
                                suspended = false;
                            ModuleLoader.step();
                            if (serialCom.update())
                                serialCom.getLightHandler().sanityCheck();
                            try {
                                Thread.sleep(10); // sleep for a while, to keep the CPU usage down.
                            } catch (InterruptedException e) {
                                System.err.println("An error occurred in the main thread.");
                                e.printStackTrace();
                            }
                        }
                        else {
                            if (!suspended) {
                                System.out.println("Suspending");
                                suspended = true;
                                Global.isSerialConnectionActive = false;
                                ModuleLoader.suspend();
                                serialCom.getLightHandler().clearBuffer();
                                if (!serialCom.halted)
                                    serialCom.close();
                            }
                            if (Global.isActive) {

                                if (serialCom.serialPortsAvailable()) {
                                    tray.setLabel("Connecting...", true);
                                    if (serialCom.initialize())
                                        tray.setLabel("", Global.isActive);
                                    else
                                        tray.setLabel("Failed to connect...", true);
                                }
                                else if (!tray.getLabel().contains("No device"))
                                    tray.setLabel("No device connected", false);
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    System.out.println("Thread sleep was interrupted.");
                                    e.printStackTrace();
                                }
                            }
                            else {
                                tray.setState(Global.isActive, "");
                                try {
                                    sleepLatch = new CountDownLatch(1);
                                    System.out.print("Awaiting latch... ");
                                    sleepLatch.await();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                System.out.println("Awoke!");
                                sleepLatch = null;
                            }
                        }
                    } catch (Exception e) {
                        MessageBox.Error(e.getCause() != null ? e.getCause().toString() : "Runtime Error!", e.getMessage()); // Displays an error box in case of something happens
                        e.printStackTrace();
                    }
                else {
                    exit(0);
                }
        }
    }

    /**
     * Private function for exiting the application and releasing all assets
     *
     * @code Error code, 0 for safe exit
     */
    private static void exit(int code) {
        if (!serialCom.close()) {
            MessageBox.Error("Serial port locked!", "WARNING: Did not exit since the application was unable to close the serial port.\nPlease disconnect the USB device and try again.\n\nThis is a safety measure, since closing the program in the current state might make the USB device unusable until force quiting the rAmbilight process");
            return;
        }
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

        if (code == 0)
            System.out.println("Exiting");
        else
            System.out.println("Exiting with error code " + code);
        System.exit(code);
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

}