package com.rambilight.core.api.Light;

import com.rambilight.core.api.Global;

/**
 * Class for handling the Lights and what to output
 */
public class LightHandler {

    private Visualizer visualizer;
    private static final int threshold = 3;

    private int diff(int a, int b) {
        return Math.abs(a - b);
    }

    /**
     * WARNING: Not allowed to be called from inside of a class.
     * Calling this will cause a runtime error when it's loaded outside the development environment.
     */
    public LightHandler(Visualizer visualizer) {
        this.visualizer = visualizer;
    }

    /**
     * Add light to the update buffer
     *
     * @param l Light position
     * @param r The amount of read
     * @param g The amount of green
     * @param b The amount of blue
     */
    public boolean addToUpdateBuffer(int l, int r, int g, int b) {
        boolean ret = false;

        for (int i = l * Global.compressionLevel; i < l * Global.compressionLevel + Global.compressionLevel; i++) {
            Light light = visualizer.getLight(i);
            if (diff(light.r, r) > threshold || diff(light.g, g) > threshold || diff(light.b, b) > threshold) {
                visualizer.setColor(i, r, g, b);
                ret = true;
            }
        }
        return ret;
    }

    /**
     * Add light to the update buffer directly. This bypasses the compression compensation
     *
     * @param l Light position
     * @param r The amount of read
     * @param g The amount of green
     * @param b The amount of blue
     */
    public boolean rawAddToUpdateBuffer(int l, int r, int g, int b) {
        Light light = visualizer.getLight(l);
        if (diff(light.r, r) > threshold || diff(light.g, g) > threshold || diff(light.b, b) > threshold) {
            visualizer.setColor(l, r, g, b);
            return true;
        }
        return false;
    }

    /**
     * Get the number of lights on a specified side.
     *
     * @param side Light position
     * @return The number of lights which are available on a specific side
     */
    public int numLightsOnSide(int side) {
        return side < numSides() ? Global.lightLayout[side] / Global.compressionLevel : 0;
    }

    /**
     * Get the number of lights on a specified side. This bypasses the compression compensation
     *
     * @param side Light position
     * @return The number of lights which are available on a specific side
     */
    public int rawNumLightsOnSide(int side) {
        return side < numSides() ? Global.lightLayout[side] : 0;
    }

    /**
     * Get the total number of lights which are available
     *
     * @return The number of lights which are available
     */
    public int numLights() {
        return Global.numLights / Global.compressionLevel;
    }

    /**
     * Get the total number of lights which are available. This bypasses the compression compensation
     *
     * @return The number of lights which are available
     */
    public int rawNumLights() {
        return Global.numLights;
    }

    /**
     * Get the total number of sides which are available.
     *
     * @return The number of sides which are available
     */
    public int numSides() {
        return Global.lightLayout.length;
    }

    /**
     * Get the current ColorBufferObject
     *
     * @return An array containing a pointer to the current color buffer object.
     * WARNING: Be careful when using this list, since modifying it might have unintended consequences.
     */
    public Light[] getColorBuffer() {
        return visualizer.getColorBuffer();
    }

}
