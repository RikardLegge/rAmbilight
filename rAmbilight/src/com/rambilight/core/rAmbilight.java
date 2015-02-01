package com.rambilight.core;

import com.rambilight.core.api.EventHandler;
import com.rambilight.core.api.Global;
import com.rambilight.core.api.ui.MessageBox;
import com.rambilight.core.api.ui.TrayController;
import com.rambilight.core.clientInterface.ComDriver;
import com.rambilight.core.clientInterface.SerialController;
import com.rambilight.core.clientInterface.debug.SerialControllerLocal;
import com.rambilight.core.clientInterface.serial.SerialControllerJSSC;
import com.rambilight.core.preferences.Preferences;
import com.rambilight.core.preferences.i18n;
import com.rambilight.plugins.Module;

import javax.swing.*;
import java.util.concurrent.CountDownLatch;

/**
 * The main/core class which initiates the rest of the application.
 */
public class rAmbilight {

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
        boolean debug = false;
        for (int k = 0; k < args.length; k++) {
            String v = args[k];
            if (v.equals("--debug"))
                debug = true;
        }
        if (debug)
            new rAmbilight().loadDebugger(null);
        else
            new rAmbilight().load();
    }

    public void loadDebugger(Class<? extends Module> debugModule) {
        load(new SerialControllerLocal(), debugModule);
    }

    public void load() {
        load(null);
    }

    public void load(Class<? extends Module> debugModule) {
        load(new SerialControllerJSSC(), debugModule);
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
            // Setup preferences and global variables
            Global.generateApplicationSupportPath();
            Preferences.setPathToDefault();
            Preferences.read();
            Global.loadPreferences();

            // Create the serial comunicator
            serialCom = new ComDriver(serialController);

            // Load extensions and modules
            if (debugModule != null)
                ModuleLoader.loadModule(debugModule);
            ModuleLoader.loadModules(ModuleLoader.loadExternalModules(rAmbilight.class));

            // Create the tray UI
            tray = new TrayController();

            // Required after the tray controller since it listens to module state changes
            for (String moduleName : Global.currentControllers)
                ModuleLoader.activateModule(moduleName);
            tray.enableRun();

            // Add an event listener to enable awaking of the main thread on active state changed.
            EventHandler.addEventListener(Global.ActiveStateModified, () -> {
                if (Global.isActive())
                    if (rAmbilight.sleepLatch != null)
                        rAmbilight.sleepLatch.countDown();
            });

            Thread thread = new Thread(new Runtime());
            thread.setName("rAmbilight Runtime");
            thread.start();
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
        }
    }

    /**
     * The infinite loop the application depends on that runs in the background
     */
    private static class Runtime implements Runnable {

        boolean suspended;

        public void run() {
            while (!Global.isRequestingExit())
                try {
                    if (Global.isSerialConnectionActive && Global.isActive()) {
                        if (suspended) {
                            suspended = false;
                            System.out.println(i18n.resuming);
                            ModuleLoader.resume();
                        }

                        ModuleLoader.step();
                        if (serialCom.update())
                            serialCom.getLightHandler().sanityCheck();
                        else if (serialCom.hasHalted) {
                            Global.setActive(false);
                            tray.setLabel(i18n.reinsertCable);
                        }
                        try {
                            Thread.sleep(10); // sleep for a while, to keep the CPU usage down.
                        } catch (InterruptedException e) {
                            System.err.println("An error occurred on the main thread.");
                            e.printStackTrace();
                        }
                    }
                    else {
                        if (!suspended) {
                            suspended = true;
                            System.out.println(i18n.suspending);
                            ModuleLoader.suspend();
                            serialCom.getLightHandler().clearBuffer();
                            serialCom.close();
                        }
                        if (Global.isActive()) {
                            if (serialCom.serialPortsAvailable()) {
                                Global.setActive(true);
                                tray.setLabel(i18n.connectingToDevice);
                                if (serialCom.initialize())
                                    tray.setLabel("");
                                else {
                                    Global.setActive(false);
                                    tray.setLabel(i18n.connectionFailed);
                                }
                            }
                            else if (!tray.getLabel().contains(i18n.noDeviceConnected))
                                tray.setLabel(i18n.noDeviceConnected, false);
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        else {
                            try {
                                sleepLatch = new CountDownLatch(1);
                                System.out.print("Application idle, Awaiting latch... ");
                                sleepLatch.await();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            System.out.println("Awoke!");
                            sleepLatch = null;

                            if (serialCom.hasHalted) {
                                serialCom.hasHalted = false;
                                Global.setActive(false);
                                tray.setLabel(i18n.reinsertCableConfirm);
                            }
                        }
                    }
                } catch (Exception e) {
                    MessageBox.Error(e.getCause() != null ? e.getCause().toString() : "Runtime Error!", e.getMessage()); // Displays an error box in case of something happens
                    e.printStackTrace();
                }
            exit(0);
        }

    }

    /**
     * Private function for exiting the application and releasing all assets
     *
     * @code Error code, 0 for safe exit
     */
    private static void exit(int code) {
        System.out.println("\nExiting...");
        try {
            ModuleLoader.dispose();
            Global.currentControllers = ModuleLoader.getActiveModules().toArray(new String[ModuleLoader.getActiveModules().size()]);
            Global.savePreferences();

            Preferences.flush();

            serialCom.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (tray != null)
                tray.remove();
        } catch (Exception e) {
            e.printStackTrace();
        }
        EventHandler.clear();

        if (code == 0)
            System.out.println("Exited");
        else
            System.out.println("Exited with error code " + code);
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

}