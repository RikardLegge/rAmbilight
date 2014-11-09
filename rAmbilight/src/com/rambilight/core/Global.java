package com.rambilight.core;

import com.legge.preferences.Preferences;

import java.io.File;

/**
 * Global class for easy handling of global variables.
 */
public class Global {

    public static final int     VERSION                = 26;
    public static final String  APPLICATIONNAME        = "rAmbilight";
    public static       boolean requestExit            = false;
    public static       boolean isActive               = true;
    public static       String  applicationSupportPath = "";

    public static int      numLights          = 60;
    public static int[]    lightLayout        = new int[]{15, 30, 15};       // Right, Top, Left, Bottom
    public static String[] currentControllers = new String[]{"Ambilight"};
    public static String   serialPort         = "";
    public static int      compressionLevel   = 1;  // Not yet implemented

    public static int     lightStepSize            = 0;
    public static boolean isSerialConnectionActive = false;
    private static Preferences preferences;

    public static void generateApplicationSupportPath() {
        String platform = System.getProperty("os.name").toLowerCase();
        if (platform.contains("win"))
            applicationSupportPath = "/AppData/Local/";
        else if (platform.contains("mac"))
            applicationSupportPath = "/Library/Application Support/";
        else
            applicationSupportPath = "/.";

        applicationSupportPath = System.getProperty("user.home") + applicationSupportPath + APPLICATIONNAME;

        if (!new File(applicationSupportPath).exists())
            new File(applicationSupportPath).mkdir();
    }

    /**
     * Loads the variables from cache
     */
    public static void loadPreferences() {
        preferences = new Preferences("Core");
        Global.isActive = preferences.load("isActive", Global.isActive);
        Global.currentControllers = preferences.load("currentControllers", Global.currentControllers, -1);

        Global.lightLayout = preferences.load("lightLayout", Global.lightLayout, -1);
        Global.lightStepSize = preferences.load("lightStepSize", Global.lightStepSize);
        Global.serialPort = preferences.load("serialPort", Global.serialPort);
        Global.compressionLevel = preferences.load("compressionLevel", Global.compressionLevel);

        numLights = 0;
        for (int num : lightLayout)
            numLights += num;
    }

    /**
     * Writes the variables to cache
     */
    public static void savePreferences() {
        preferences.save("isActive", Global.isActive);
        preferences.save("currentControllers", Global.currentControllers);

        preferences.save("lightLayout", lightLayout);

        preferences.save("lightStepSize", lightStepSize);
        preferences.save("serialPort", Global.serialPort);
        preferences.save("lightStepSize", Global.lightStepSize);

        preferences.save("VERSION", Global.VERSION);
    }
}
