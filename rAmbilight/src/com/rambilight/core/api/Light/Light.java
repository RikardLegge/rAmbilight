package com.rambilight.core.api.Light;

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

    public void set(int r, int g, int b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }
}