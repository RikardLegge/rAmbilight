package com.rambilight.core.api.Light;

import com.rambilight.core.Main;
import com.rambilight.core.api.Global;
import com.rambilight.core.serial.LightHandlerCore;

/**
 * Class for handling the Lights and what to output
 */
public class LightHandler {

    LightHandlerCore lightHandlerCore;
    String           name;

    public LightHandler(String name) {
        this.name = name;
        this.lightHandlerCore = Main.getSerialCom().getLightHandler();
        lightHandlerCore.registerModule(name);
    }

    public boolean addToUpdateBuffer(int id, int r, int g, int b) {
        return lightHandlerCore.addToUpdateBuffer(name, id, r, g, b);
    }

    public boolean rawAddToUpdateBuffer(int id, int r, int g, int b) {
        return lightHandlerCore.addToUpdateBuffer(name, id, r, g, b);
    }

    public int numLightsOnSide(int side) {
        return side < numSides() ? Global.lightLayout[side] / Global.compressionLevel : 0;
    }

    public int numLights() {
        return lightHandlerCore.getNumLights() / Global.compressionLevel;
    }

    public int rawNumLightsOnSide(int side) {
        return side < numSides() ? Global.lightLayout[side] : 0;
    }

    public int rawNumLights() {
        return lightHandlerCore.getNumLights();
    }

    public int numSides() {
        return Global.lightLayout.length;
    }

    public Light[] getColorBuffer() {
        return lightHandlerCore.getColorBuffer();
    }

    public int getNumLightsToUpdate() {
        return lightHandlerCore.getNumLightsToUpdate();
    }
}
