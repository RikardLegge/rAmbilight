package com.rambilight.core.api;

import com.legge.preferences.Preferences;

import java.io.File;
import java.util.ArrayList;

public class Global {

    public static final ArrayList<String> ERRORLOG        = new ArrayList<>();
    public static final int               VERSION         = 30;
    public static final String            APPLICATIONNAME = "rAmbilight";

    public static boolean requestExit            = false;
    public static boolean disableErrorPopups     = false;
    public static boolean isActive               = true;
    public static String  applicationSupportPath = "";

    public static int      numLights                   = 0;
    public static int[]    lightLayout                 = new int[]{15, 30, 15};
    public static boolean  lightLayoutClockwise        = true;
    public static int      lightLayoutStartingPosition = 2;
    public static String[] currentControllers          = new String[]{"Ambilight"};
    public static String   serialPort                  = "";
    public static int      compressionLevel            = 1;
    public static boolean  compressionAutoSet          = true;

    public static int     lightStepSize            = 0;
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
        preferences.save("lightLayout", lightLayout);

        preferences.save("lightStepSize", lightStepSize);
        preferences.save("lightStepSize", Global.lightStepSize);
        preferences.save("serialPort", Global.serialPort);
        preferences.save("compressionLevel", Global.compressionLevel);
        preferences.save("compressionAutoSet", Global.compressionAutoSet);

        preferences.save("VERSION", Global.VERSION);
    }
}
