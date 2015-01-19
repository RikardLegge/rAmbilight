package com.rambilight.plugins.Built_In_Effects.extensions;

import com.rambilight.core.api.Light.LightHandler;
import com.rambilight.plugins.Built_In_Effects.Effect;

public class KnightRider extends Effect {

    int     index     = 0;
    boolean direction = true;
    int     width     = 10;

    public String getName() {
        return "Knight Rider";
    }

    public void step(LightHandler lightHandler) {
        if (index >= lightHandler.numLightsOnSide(0) + lightHandler.numLightsOnSide(1) - 1) {
            direction = false;
            index = lightHandler.numLightsOnSide(0) + lightHandler.numLightsOnSide(1) - width;
        }
        else if (index <= lightHandler.numLightsOnSide(0) + 1) {
            direction = true;
            index = lightHandler.numLightsOnSide(0) + width;
        }

        if (direction) {
            index++;
            lightHandler.addToUpdateBuffer(index - width * 2, 0, 0, 0);
            lightHandler.addToUpdateBuffer(index, 250, 0, 0);
        }
        else {
            index--;
            lightHandler.addToUpdateBuffer(index + width * 2, 0, 0, 0);
            lightHandler.addToUpdateBuffer(index, 250, 0, 0);
        }
    }
}
