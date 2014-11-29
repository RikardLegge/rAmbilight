package com.rambilight.plugins.Built_In_Effects.extensions;

import com.rambilight.core.api.Light.LightHandler;
import com.rambilight.plugins.Built_In_Effects.Effect;

public class CirclingLights extends Effect {

    int index = 0;

    public String getName() {
        return "Circling Lights";
    }

    public void step(LightHandler lightHandler) {
        if (index - 4 < 0)
            lightHandler.addToUpdateBuffer(lightHandler.numLights() - 4 + index, 0, 0, 0);
        else
            lightHandler.addToUpdateBuffer(index - 4, 0, 0, 0);
        lightHandler.addToUpdateBuffer(index, 200, 200, 200);

        index = (index + 1) % lightHandler.numLights();
    }
}
