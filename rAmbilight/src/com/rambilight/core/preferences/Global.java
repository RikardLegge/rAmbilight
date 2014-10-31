package com.rambilight.core.preferences;

/** Global class for easy handling of global variables. */
public class Global {

    public static final int    VERSION            = 25;
    public static boolean      requestExit        = false;
    public static boolean      isActive           = false;

    public static int          numLights          = 59;
    public static int[]        lightLayout        = new int[] { 15, 29, 15 };       // Right, Top, Left, Bottom
    public static String[]     currentControllers = new String[0];
    public static String       serialPort         = "/dev/tty.usbmodem1451";//"COM3";

    public static String       pluginPath         = "";
    public static boolean      loadInternal       = false;
    public static int          lightStepSize      = 6;
    public static int          lightFrameDelay    = 6;
    private static Preferences preferences        = new Preferences("core");

    public static final String PLATFORM = System.getProperty("os.name").toLowerCase();

    /** Loads the variables from cache */
    public static void loadPreferences() {
        preferences = new Preferences("core");
        Global.isActive = preferences.load("isActive", Global.isActive);
        Global.currentControllers = preferences.load("currentControllers", Global.currentControllers, -1);

        Global.lightLayout = preferences.load("lightLayout", Global.lightLayout, -1);
        Global.loadInternal = preferences.load("loadInternal", Global.loadInternal);
        Global.pluginPath = preferences.load("pluginPath", Global.pluginPath);

        Global.lightStepSize = preferences.load("lightStepSize", Global.lightStepSize);
        Global.lightFrameDelay = preferences.load("lightFrameDelay", Global.lightFrameDelay);
        Global.serialPort = preferences.load("serialPort", Global.serialPort);

        numLights = 0;
        for (int num : lightLayout)
            numLights += num;
    }

    /** Writes the variables to cache */
    public static void savePreferences() {
        preferences.save("isActive", Global.isActive);
        preferences.save("currentControllers", Global.currentControllers);

        preferences.save("lightLayout", lightLayout);
        preferences.save("loadInternal", Global.loadInternal);
        preferences.save("pluginPath", Global.pluginPath);

        preferences.save("lightStepSize", lightStepSize);
        preferences.save("lightFrameDelay", Global.lightFrameDelay);
        preferences.save("serialPort", Global.serialPort);

        preferences.save("VERSION", Global.VERSION);
    }
}
