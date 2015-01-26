package com.rambilight.core;

import com.rambilight.core.preferences.Preferences;
import com.rambilight.core.api.Global;
import com.rambilight.core.api.ui.MessageBox;
import com.rambilight.core.api.ui.TrayController;
import com.rambilight.core.clientInterface.ComDriver;
import com.rambilight.core.clientInterface.SerialController;
import com.rambilight.core.clientInterface.debug.SerialControllerLocal;
import com.rambilight.core.clientInterface.serial.SerialControllerJSSC;
import com.rambilight.plugins.Module;

import javax.swing.*;
import java.util.concurrent.CountDownLatch;

/**
 * The main/core class which initiates the rest of the application.
 */
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
        new Main().load();
    }

    public void loadDebugger(Class<? extends Module> debugModule) {
        load(new SerialControllerLocal(), debugModule);
    }

    public void load() {
        load(new SerialControllerJSSC(), null);
    }

    private void load(SerialController serialController, Class<? extends Module> debugModule) {

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

            serialCom = new ComDriver(serialController);

            if (debugModule != null)
                ModuleLoader.loadModule(debugModule);

            ModuleLoader.loadModules(ModuleLoader.loadExternalModules(Main.class));

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
        thread.start();
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
                                ModuleLoader.suspend();
                                serialCom.getLightHandler().clearBuffer();
                                serialCom.close();
                            }
                            if (Global.isActive) {

                                if (serialCom.serialPortsAvailable()) {
                                    tray.setLabel("Connecting...", true);
                                    if (serialCom.initialize())
                                        tray.setLabel("", Global.isActive);
                                    else {
                                        Global.isActive = false;
                                        tray.setState(Global.isActive, "Failed to connect...");
                                    }
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