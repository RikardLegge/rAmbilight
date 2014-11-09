package com.rambilight.core.api.Light;

import com.rambilight.core.Main;
import com.rambilight.core.serial.LightHandlerCore;

/**
 * Class for handling the Lights and what to output
 */
public class LightHandler {

    LightHandlerCore lightHandler;
    String           name;

    public LightHandler(String name) {
        this.name = name;
        this.lightHandler = Main.getSerialCom().getLightHandler();
        lightHandler.registerModule(name);
    }

    public boolean addToUpdateBuffer(int id, int r, int g, int b) {
        return lightHandler.addToUpdateBuffer(name, id, r, g, b);
    }

    public Light[] getColorBuffer() {
        return lightHandler.getColorBuffer();
    }

    public int getNumLightsToUpdate() {
        return lightHandler.getNumLightsToUpdate();
    }
}
