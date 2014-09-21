package com.rambilight.core.preferences;

/** Global class for easy handling of global variables. */
public class Global {

    public static final int VERSION = -1; // The current version of the application. Can help with debugging.
    public static boolean   requestExit; // If set to true, the application will try to exit gracefully.
    public static boolean   isActive = true;    // Is the application active. If set to false, the user might get confused.
    public static int       numLights = 59;   // The number of lights which are available
}
