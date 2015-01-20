package com.rambilight.plugins.Built_In_Effects.extensions;

import com.rambilight.core.api.Light.LightHandler;
import com.rambilight.plugins.Built_In_Effects.Effect;

public class Flashing extends Effect {

    int state = 0;

    public String getName() {
        return "Flashing";
    }

    public void step(LightHandler lightHandler) {
        int r = 5 * (state++ % 5);
        int g = 4 * (state++ % 6);
        int b = 30 * (state++ % 7);
        for (int i = 0; i < lightHandler.numLights(); i++)
            lightHandler.addToUpdateBuffer(i, r, g, b);
    }
}
