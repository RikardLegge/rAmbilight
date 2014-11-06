package com.rambilight.core;

import com.legge.preferences.Preferences;

/**
 * Global class for easy handling of global variables.
 */
public class Global {

    public static final int     VERSION         = 26;
    public static       boolean requestExit     = false;
    public static       boolean isActive        = true;
    public static       String  preferencesPath = "";

    public static int      numLights          = 59;
    public static int[]    lightLayout        = new int[]{15, 30, 15};       // Right, Top, Left, Bottom
    public static String[] currentControllers = new String[]{"Ambilight"};
    public static String   serialPort         = "";

    public static String  pluginPath    = "";
    public static boolean loadInternal  = false;
    public static int     lightStepSize = 0;
    private static Preferences preferences;

    /**
     * Loads the variables from cache
     */
    public static void loadPreferences() {
        preferences = new Preferences("Core");
        Global.isActive = preferences.load("isActive", Global.isActive);
        Global.currentControllers = preferences.load("currentControllers", Global.currentControllers, -1);

        Global.lightLayout = preferences.load("lightLayout", Global.lightLayout, -1);
        Global.loadInternal = preferences.load("loadInternal", Global.loadInternal);
        Global.pluginPath = preferences.load("pluginPath", Global.pluginPath);

        Global.lightStepSize = preferences.load("lightStepSize", Global.lightStepSize);
        Global.serialPort = preferences.load("serialPort", Global.serialPort);

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
        preferences.save("loadInternal", Global.loadInternal);
        preferences.save("pluginPath", Global.pluginPath);

        preferences.save("lightStepSize", lightStepSize);
        preferences.save("serialPort", Global.serialPort);

        preferences.save("VERSION", Global.VERSION);
    }
}
