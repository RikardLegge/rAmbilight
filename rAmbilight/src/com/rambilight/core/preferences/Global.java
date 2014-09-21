package com.rambilight.core.preferences;

/** Global class for easy handling of global variables. */
public class Global {

    public static final int    VERSION            = 24;
    public static boolean      requestExit        = false;
    public static boolean      isActive           = false;

    public static int          numLights          = 59;
    public static String[]     currentControllers = new String[0];
    public static String       serialPort         = "COM3";

    public static String       pluginPath         = "/plugins";
    public static boolean      loadInternal       = false;
    public static int          lightStepSize      = 10;
    public static int          lightFrameDelay    = 10;
    private static Preferences preferences        = new Preferences("core");

    /** Loads the variables from cache */
    public static void loadPreferences() {
        preferences = new Preferences("core");
        Global.isActive = preferences.load("isActive", Global.isActive);
        Global.currentControllers = preferences.load("currentControllers", Global.currentControllers, -1);

        Global.numLights = preferences.load("numLights", Global.numLights);
        Global.loadInternal = preferences.load("loadInternal", Global.loadInternal);
        Global.pluginPath = preferences.load("pluginPath", Global.pluginPath);

        Global.lightStepSize = preferences.load("lightStepSize", Global.lightStepSize);
        Global.lightFrameDelay = preferences.load("lightFrameDelay", Global.lightFrameDelay);
        Global.serialPort = preferences.load("serialPort", Global.serialPort);
    }

    /** Writes the variables to cache */
    public static void savePreferences() {
        preferences.save("isActive", Global.isActive);
        preferences.save("currentControllers", Global.currentControllers);

        preferences.save("numLights", Global.numLights);
        preferences.save("loadInternal", Global.loadInternal);
        preferences.save("pluginPath", Global.pluginPath);

        preferences.save("VERSION", Global.VERSION);
    }
}
