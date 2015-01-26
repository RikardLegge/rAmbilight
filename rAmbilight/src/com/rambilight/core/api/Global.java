package com.rambilight.core.api;

import com.rambilight.core.preferences.Preferences;

import java.io.File;
import java.util.ArrayList;

/**
 * Global class for easy handling of global variables.
 * It's not recommended to use these values directly,
 * since parts like the LightHandler applies compensation
 * for the compression level.
 * <p>
 * Only use these values if absolutely necessary.
 * See the LightHandler class for a more efficient way
 * of using most these value.
 */
public class Global {

    public static final ArrayList<String> ERRORLOG        = new ArrayList<>();
    public static final String            APPLICATIONNAME = "rAmbilight";
    /**
     * The version number of the API and application.
     */
    public static final int               VERSION         = 31;

    /**
     * If set to true, the application will try to exit gracefully.
     */
    public static boolean requestExit            = false;
    public static boolean disableErrorPopups     = false;
    /**
     * Is the application active?
     * WARNING: If set to false manually, the user might get confused.
     * Only change this value if you know what you are doing.
     */
    public static boolean isActive               = true;
    public static String  applicationSupportPath = "";

    /**
     * The number of lights which are available, calculated from the
     * light layout.
     */
    public static int      numLights                   = 0;
    /**
     * The light layout of the screen, in order of which they appear.
     * This only effects modules which require control over specific
     * sides of the screen, not only which light is active.
     * <p>
     * Example:
     * {15, 30, 15, 30};
     * This says that the first side has 15 lights, the second has 30,
     * the third 15 and the forth 30.
     */
    public static int[]    lightLayout                 = new int[]{15, 30, 15};
    /**
     * The direction of which the lights get addressed.
     * This only effects modules which require control over specific
     * sides of the screen, not only which light is active.
     */
    public static boolean  lightLayoutClockwise        = true;
    /**
     * The starting position of which the lights get addressed.
     * This only effects modules which require control over specific
     * sides of the screen, not only which light is active.
     */
    public static int      lightLayoutStartingPosition = 2;
    public static String[] currentControllers          = new String[]{"Ambilight"};
    public static String   serialPort                  = "";
    /**
     * The amount of compression which is applied to the data using
     * a simple algorithm that only takes every n:th lights and fills
     * the gap with a color related to the nearest neighbours.
     */
    public static int      compressionLevel            = 1;
    public static boolean  compressionAutoSet          = true;

    public static int     lightUpdateThreshold     = 3;
    public static int     lightStepSize            = 8;
    public static boolean isSerialConnectionActive = false;

    private static Preferences preferences;

    public static void generateApplicationSupportPath() {
        applicationSupportPath = Platform.getApplicationSupportPath(APPLICATIONNAME);
        if (!new File(applicationSupportPath).exists())
            new File(applicationSupportPath).mkdir();
    }

    public static void loadPreferences() {
        preferences = new Preferences("Core");
        Global.isActive = preferences.load("isActive", Global.isActive);
        Global.disableErrorPopups = preferences.load("disableErrorPopups", Global.disableErrorPopups);
        Global.currentControllers = preferences.load("currentControllers", Global.currentControllers, -1);

        Global.lightLayoutClockwise = preferences.load("lightLayoutClockwise", Global.lightLayoutClockwise);
        Global.lightLayoutStartingPosition = preferences.load("lightLayoutStartingPosition", Global.lightLayoutStartingPosition);
        Global.lightLayout = preferences.load("lightLayout", Global.lightLayout, -1);
        numLights = 0;
        for (int num : lightLayout)
            numLights += num;

        Global.lightUpdateThreshold = preferences.load("lightUpdateThreshold", Global.lightUpdateThreshold);
        Global.lightStepSize = preferences.load("lightStepSize", Global.lightStepSize);
        Global.serialPort = preferences.load("serialPort", Global.serialPort);
        Global.compressionAutoSet = preferences.load("compressionAutoSet", Global.compressionAutoSet);
        if (compressionAutoSet)
            Global.compressionLevel = (int) Math.floor(Global.numLights / 50);
        else
            Global.compressionLevel = preferences.load("compressionLevel", Global.compressionLevel);
        Global.compressionLevel = Global.compressionLevel > 0 ? Global.compressionLevel : 1;
    }

    public static void savePreferences() {
        preferences.save("isActive", Global.isActive);
        preferences.save("disableErrorPopups", Global.disableErrorPopups);
        preferences.save("currentControllers", Global.currentControllers);

        preferences.save("lightLayoutClockwise", Global.lightLayoutClockwise);
        preferences.save("lightLayoutStartingPosition", Global.lightLayoutStartingPosition);
        preferences.save("lightLayout", Global.lightLayout);

        preferences.save("lightUpdateThreshold", Global.lightUpdateThreshold);
        preferences.save("lightStepSize", Global.lightStepSize);
        preferences.save("serialPort", Global.serialPort);
        preferences.save("compressionLevel", Global.compressionLevel);
        preferences.save("compressionAutoSet", Global.compressionAutoSet);

        preferences.save("VERSION", Global.VERSION);
    }
}
