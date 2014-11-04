package com.rambilight.core;

/** Global class for easy handling of global variables. */
public class Global {

    public static final int VERSION     = 26;                      // The current version of the application. Can help with debugging.
    public static boolean   requestExit;                           // If set to true, the application will try to exit gracefully.
    public static boolean   isActive    = true;                    // Is the application active. If set to false, the user might get confused.
    public static int[]     lightLayout = new int[] { 15, 30, 15, 30 };// Right, Top, Left, Bottom
    public static int       numLights   = 60+30;                      // The number of lights which are available. (Calculated from lightLayout)
}
