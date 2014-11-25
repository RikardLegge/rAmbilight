package com.rambilight.core.api.Light;

/*
 * The class which wraps each Light into a manageable packet.
 */
public class Light {

    public int     id;
    public int     r;
    public int     g;
    public int     b;
    public boolean requiresUpdate;

    public Light(int id, int r, int g, int b) {
        this.id = id;
        this.r = r;
        this.g = g;
        this.b = b;
        this.requiresUpdate = false;
    }

    /**
     * Change the color or the specified light
     *
     * @param r RED (0 - 252)
     * @param g GREEN (0 - 252)
     * @param b BLUE (0 - 252)
     */
    public void set(int r, int g, int b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }
}