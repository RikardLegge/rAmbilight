package com.rambilight.core.api.Light;

/**
 * Class for handling the Lights and what to output
 */
public class LightHandler {

    private Visualizer visulizer;
    private static final int threshhold = 3;

    private int diff(int a, int b) {
        return Math.abs(a - b);
    }

    /**
     * WARNING: Not allowed to be called from inside of a class
     */
    public LightHandler(Visualizer visulizer) {
        this.visulizer = visulizer;
    }

    /**
     * Add light to the update buffer
     *
     * @param l Lightposition
     * @param r The amount of read
     * @param g The amount of green
     * @param b The amount of blue
     */
    public boolean addToUpdateBuffer(int l, int r, int g, int b) {
        Light light = visulizer.getLight(l);
        if (diff(light.r, r) > threshhold || diff(light.g, g) > threshhold || diff(light.b, b) > threshhold) {
            visulizer.setColor(l, r, g, b);
            return true;
        }
        return false;
    }

    /**
     * Get the current ColorBufferObject
     *
     * @return An array containing a pointer to the current color buffer object. Be careful when using this, since modifying the list might have unintended consequences.
     */
    public Light[] getColorBuffer() {
        return visulizer.getColorBuffer();
    }

}
