package com.rambilight.core.api.Light;

import com.rambilight.core.api.Global;
import com.rambilight.core.clientInterface.LightHandlerCore;
import com.rambilight.core.rAmbilight;

/**
 * A class for handling the Lights and what to output to the ambilight device.
 * This class is a wrapper around the lightHandlerCore and creates a manageable interface
 * for modules to communicate by.
 */

public class LightHandler {

	private LightHandlerCore lightHandlerCore;
	private String           name;

	private int     compressionLevel;
	private boolean clockwiseLayout;
	private int     sideOffset;

	private int   offset;
	private int[] lightLayout;

	/**
	 * Shouldn't ever be needed to call from within modules, but if the time comes creates a new interface for handling light output.
	 *
	 * @param nameid Name of the client. (The caller class name might be used in the future. )
	 */
	public LightHandler(String nameid) {
		name = nameid;

		compressionLevel = Global.compressionLevel;
		clockwiseLayout = Global.lightLayoutClockwise;
		sideOffset = Global.lightLayoutOffset;

		lightHandlerCore = rAmbilight.getSerialCom().getLightHandler();
		offset = -Global.lightOffset;
		lightLayout = new int[Global.lightLayout.length];

		lightHandlerCore.registerModule(name);

		for (int i = 0; i < Global.lightLayout.length; i++) {
			lightLayout[getSideByIndex(i)] = Global.lightLayout[i];
		}

		for (int i = 0; i < sideOffset; i++) {
			offset += numLightsOnSide(i);
		}
	}

	/**
	 * Add light to the update buffer.
	 * The color properties are BYTE values in INT form for easy manipulation.
	 * They should therefor be a value between 0 and 252
	 * (The last three available bytes are reserved for the communication protocol).
	 * A higher or lower value than this be cut to 0 / 252.
	 *
	 * @param id The position of the light.
	 * @param r  The amount of red
	 * @param g  The amount of green
	 * @param b  The amount of blue
	 * @return true if the value was set as a new one.
	 */
	public boolean addToUpdateBuffer(int id, int r, int g, int b) {
		id = (id + offset) % rawNumLights();
		boolean ret = false;
		for (int i = id * compressionLevel; i < id * compressionLevel + compressionLevel; i++) {
			if (lightHandlerCore.addToUpdateBuffer(name, i, r, g, b))
				ret = true;
		}
		return ret;
	}

	/**
	 * Add light to the update buffer directly.
	 * This bypasses the compression compensation.
	 *
	 * @param id Light position
	 * @param r  The amount of red
	 * @param g  The amount of green
	 * @param b  The amount of blue
	 * @return true if the value was set as a new one.
	 */
	public boolean rawAddToUpdateBuffer(int id, int r, int g, int b) {
		return lightHandlerCore.addToUpdateBuffer(name, id, r, g, b);
	}

	/**
	 * Get the number of lights on a specified side.
	 *
	 * @param side Light position
	 * @return The number of lights which are available on a specific side
	 */
	public int numLightsOnSide(int side) {
		return side < numSides() ? lightLayout[side] / compressionLevel : 0;
	}

	/**
	 * Get the total number of lights which are available
	 *
	 * @return The number of lights which are available
	 */
	public int numLights() {
		return lightHandlerCore.getNumLights() / compressionLevel;
	}

	/**
	 * Get the number of lights on a specified side. This bypasses the compression compensation
	 *
	 * @param side Light position
	 * @return The number of lights which are available on a specific side
	 */
	public int rawNumLightsOnSide(int side) {
		return side < numSides() ? lightLayout[side] : 0;
	}

	/**
	 * Get the total number of lights which are available
	 *
	 * @return The number of lights which are available
	 */
	public int rawNumLights() {
		return lightHandlerCore.getNumLights();
	}

	/**
	 * Get the total number of sides which are available.
	 *
	 * @return The number of sides which are available
	 */
	public int numSides() {
		return lightLayout.length;
	}

	public int[] getLayout() {
		return lightLayout;
	}

	/**
	 * Get the side which is compensated by the direction and starting position.
	 * This should be used if the plugin requires to know which side each side index corresponds to.
	 * Without this, It's a lot harder to know which side is which in all different setups.
	 *
	 * @param pos A value between zero and the number of sides which are available.
	 * @return The real side a static indexed side actually relates to.
	 */
	private int getSideByIndex(int pos) {
		int mod = numSides();
		int val = (clockwiseLayout ? -pos : pos) - sideOffset;

		return ((val % mod) + mod) % mod;
	}

	/**
	 * Get the current ColorBufferObject
	 *
	 * @return An array containing a pointer to the current color buffer object.
	 * WARNING: Be careful when using this list, since modifying it might have unintended consequences.
	 */
	public Light[] getColorBuffer() {
		return lightHandlerCore.getColorBuffer();
	}

	/**
	 * Get the number of lights which will be updated on the next communication cycle.
	 *
	 * @return number of lights
	 */
	public int getNumLightsToUpdate() {
		return lightHandlerCore.getNumLightsToUpdate();
	}
}
