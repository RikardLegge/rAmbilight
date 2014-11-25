package com.rambilight.core.api;

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
    /**
     * The version number of the API and application.
     */
    public static final int VERSION = 30;
    /**
     * If set to true, the application will try to exit gracefully.
     */
    public static boolean requestExit;
    /**
     * Is the application active?
     * WARNING: If set to false manually, the user might get confused.
     * Only change this value if you know what you are doing.
     */
    public static boolean isActive                    = true;
    /**
     * The direction of which the lights get addressed.
     * This only effects modules which require control over specific
     * sides of the screen, not only which light is active.
     */
    public static boolean lightLayoutClockwise        = true;
    /**
     * The starting position of which the lights get addressed.
     * This only effects modules which require control over specific
     * sides of the screen, not only which light is active.
     */
    public static int     lightLayoutStartingPosition = 0;
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
    public static int[]   lightLayout                 = new int[]{34, 58, 35, 53};
    /**
     * The number of lights which are available, calculated from the
     * light layout.
     */
    public static int     numLights                   = 0;
    /**
     * The amount of compression which is applied to the data using
     * a simple algorithm that only takes every n:th lights and fills
     * the gap with a color related to the nearest neighbours.
     */
    public static int     compressionLevel            = 2;

    /**
     * WARNING: The visualizer is not available outside of the API environment.
     * Therefor, don't call it from within a module, since this would cause it to be invalidated!
     */
    public static void SETUP() {
        numLights = 0;
        for (int num : lightLayout)
            numLights += num;
    }
}
