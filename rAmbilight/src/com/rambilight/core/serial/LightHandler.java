package com.rambilight.core.serial;

import com.rambilight.core.AmbilightDriver;

/**
 * Class for handling the Lights and what to output
 */
public class LightHandler {

    LightHandlerCore lightHandler;
    String           name;

    public LightHandler(String name) {
        this.name = name;
        this.lightHandler = AmbilightDriver.getSerialCom().getLightHandler();
        lightHandler.registerModule(name);
    }

    public void addToUpdateBuffer(int id, int r, int g, int b) {
        lightHandler.addToUpdateBuffer(name, id, r, g, b);
    }

    public Light[] getColorBuffer() {
        return lightHandler.getColorBuffer();
    }
}
