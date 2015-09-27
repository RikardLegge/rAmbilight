package com.rambilight.plugins.Built_In_Effects.extensions;

import com.rambilight.core.api.Light.LightHandler;
import com.rambilight.core.api.Side;
import com.rambilight.plugins.Built_In_Effects.Effect;

public class KnightRider extends Effect {

	int     index     = 0;
	boolean direction = true;
	int     width     = 5;

	public String getName() {
		return "Knight Rider";
	}

	public void step(LightHandler lightHandler) {
		if (index >= lightHandler.numLightsOnSide(Side.RIGHT) + lightHandler.numLightsOnSide(Side.TOP) - 1) {
			direction = false;
			index = lightHandler.numLightsOnSide(Side.RIGHT) + lightHandler.numLightsOnSide(Side.TOP) - width;
		} else if (index <= lightHandler.numLightsOnSide(Side.RIGHT) + 1) {
			direction = true;
			index = lightHandler.numLightsOnSide(Side.RIGHT) + width;
		}

		if (direction) {
			index++;
			lightHandler.addToUpdateBuffer(index - width * 2, 0, 0, 0);
			lightHandler.addToUpdateBuffer(index, 250, 0, 0);
		} else {
			index--;
			lightHandler.addToUpdateBuffer(index + width * 2, 0, 0, 0);
			lightHandler.addToUpdateBuffer(index, 250, 0, 0);
		}
	}
}
