package com.rambilight.core.api;

/**
 * Global class for easy handling of global variables.
 * It's not recommended to use these values directly, since parts like the LightHandler applies compensation for the compression level.
 * <p>
 * Only use these values if absolutely necessary.
 */
public class Global {
    public static final int VERSION = 29;                                   // The current version of the application. Can help with debugging.
    public static boolean requestExit;                                      // If set to true, the application will try to exit gracefully.
    public static boolean isActive         = true;                          // Is the application active. If set to false, the user might get confused.
    public static int[]   lightLayout      = new int[]{33, 56, 41};     // Right, Top, Left, Bottom
    public static int     numLights        = 130;                           // The number of lights which are available. (Calculated from lightLayout)
    public static int     compressionLevel = 2;                             // The amount of compression which is applied to the data
}
