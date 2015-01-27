package com.rambilight.core.clientInterface;

import com.rambilight.core.ModuleLoader;
import com.rambilight.core.api.Global;
import com.rambilight.core.api.Light.Light;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Queue;

public class LightHandlerCore {

    private int numLights;                                      // Total number of lights

    private Light[]                    colorBuffer;             // List of the colors of the respective lights
    private Queue<Integer>             lightsToUpdate;          // List of lights that require updating
    private Hashtable<String, Light[]> identifiableColorBuffer;


    public LightHandlerCore(int numLights) {
        this.numLights = numLights;
        lightsToUpdate = new LinkedList<>();
        identifiableColorBuffer = new Hashtable<>();
        colorBuffer = new Light[numLights];
        for (int i = 0; i < colorBuffer.length; i++)
            colorBuffer[i] = new Light(i, (byte) 0, (byte) 0, (byte) 0);
        ModuleLoader.addOnChangeListener((s) -> {
            for (int i = 0; i < colorBuffer.length; i++) {
                lightsToUpdate.add(i);
                colorBuffer[i].requiresUpdate = true;
            }
        });
    }

    public void registerModule(String name) {
        if (!identifiableColorBuffer.contains(name)) {
            Light[] buffer = new Light[numLights];
            for (int i = 0; i < numLights; i++)
                buffer[i] = new Light(i, (byte) 0, (byte) 0, (byte) 0);

            identifiableColorBuffer.put(name, buffer);
        }
    }


    public boolean addToUpdateBuffer(String name, int id, int r, int g, int b) {
        return addToUpdateBuffer(name, id, r, g, b, false);
    }

    private boolean addToUpdateBuffer(String name, int id, int r, int g, int b, boolean force) {

        id = id < getNumLights() ? id : getNumLights();
        id = id - (id % Global.compressionLevel);    // Set the ID to static values. Ex. x % 2 > 0,2,4,...

        Light light = identifiableColorBuffer.get(name)[id];

        /*if (colorBuffer[id].requiresUpdate) {
            r = (light.r + r) / 2;
            g = (light.g + g) / 2;
            b = (light.b + b) / 2;
        }*/

        r = Math.max(Math.min(r, 250), 0);
        g = Math.max(Math.min(g, 250), 0);
        b = Math.max(Math.min(b, 250), 0);

        if (enoughDiff(light.r, r) || enoughDiff(light.g, g) || enoughDiff(light.b, b) || force) {
            light.r = r;
            light.g = g;
            light.b = b;
            if (!colorBuffer[id].requiresUpdate) {
                lightsToUpdate.add(id);
                colorBuffer[id].requiresUpdate = true;
            }
            return true;
        }
        return false;
    }

    public boolean requiresUpdate() {
        return lightsToUpdate.peek() != null;
    }

    public int getNumLightsToUpdate() {
        return lightsToUpdate.size();
    }


    public void remove() {
        if (requiresUpdate()) {
            Integer lightid = lightsToUpdate.remove();
            if (lightid == null)
                return;
            colorBuffer[lightid].requiresUpdate = false;
        }
    }

    public Light next() {
        if (requiresUpdate()) {
            Integer lightid = lightsToUpdate.remove();
            if (lightid == null)
                return next();
            Light nextLight = composeColor(lightid);
            nextLight.requiresUpdate = false;
            return nextLight;
        }
        return null;
    }

    public Light peek() {
        if (requiresUpdate()) {
            Integer lightid = lightsToUpdate.peek();
            if (lightid == null)
                return peek();
            Light nextLight = composeColor(lightid);
            nextLight.requiresUpdate = false;
            return nextLight;
        }
        return null;
    }


    public int getNumLights() {
        return numLights;
    }

    public Light[] getColorBuffer() {
        return colorBuffer;
    }

    private Light composeColor(int i) {
        colorBuffer[i].set(252, 252, 252);
        if (ModuleLoader.getActiveModules().size() == 0)
            colorBuffer[i].set(0, 0, 0);
        else
            ModuleLoader.getActiveModules().parallelStream().filter(identifiableColorBuffer::containsKey).forEach(name -> {
                Light lightI = identifiableColorBuffer.get(name)[i];
                colorBuffer[i].r = Math.round((float) colorBuffer[i].r * ((float) lightI.r / 252f));
                colorBuffer[i].g = Math.round((float) colorBuffer[i].g * ((float) lightI.g / 252f));
                colorBuffer[i].b = Math.round((float) colorBuffer[i].b * ((float) lightI.b / 252f));
            });
        return colorBuffer[i];
    }

    private int diff(int a, int b) {
        return Math.abs(a - b);
    }

    private boolean enoughDiff(int light, int val) {
        return //!(diff(light, val) < Global.lightUpdateThreshold && light > Global.lightUpdateThreshold);
                diff(light, val) > Global.lightUpdateThreshold || (val == 0 && light > 0);
    }

    public void clearBuffer() {
        lightsToUpdate.clear();
        for (Light light : colorBuffer)
            light.requiresUpdate = false;
        for (String key : identifiableColorBuffer.keySet())
            for (Light light : identifiableColorBuffer.get(key)) {
                light.r = 0;
                light.g = 0;
                light.b = 0;
            }
    }

    public void sanityCheck() {
        if (lightsToUpdate.size() > 0 && lightsToUpdate.peek() == null) {
            lightsToUpdate.clear();
            for (Light light : colorBuffer)
                light.requiresUpdate = false;
        }
    }

    public void reset() {
        lightsToUpdate.clear();
        for (int i = 0; i < numLights; i++)
            lightsToUpdate.add(i);
    }

}
