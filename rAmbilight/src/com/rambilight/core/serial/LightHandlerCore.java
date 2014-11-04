package com.rambilight.core.serial;

import com.rambilight.core.ModuleLoader;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Queue;

public class LightHandlerCore {

    private int                        numLights;              // Total numbe of lights
    private Light[]                    colorBuffer;            // List of the colors of the respective lights
    private Queue<Integer>             lightsToUpdate;         // List of lights that require updating
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

    public boolean requiresUpdate() {
        return lightsToUpdate.peek() != null;
    }

    public Light next() {
        if (requiresUpdate())
            return composeColor(lightsToUpdate.poll());
        return null;
    }

    private Light composeColor(int i) {
        colorBuffer[i].set(252, 252, 252);
        if (ModuleLoader.getActiveModules().size() == 0)
            colorBuffer[i].set(0, 0, 0);
        else
            ModuleLoader.getActiveModules().parallelStream().filter(name -> identifiableColorBuffer.containsKey(name)).forEach(name -> {
                Light lightI = identifiableColorBuffer.get(name)[i];
                colorBuffer[i].r = Math.round((float) colorBuffer[i].r * ((float) lightI.r / 252f));
                colorBuffer[i].g = Math.round((float) colorBuffer[i].g * ((float) lightI.g / 252f));
                colorBuffer[i].b = Math.round((float) colorBuffer[i].b * ((float) lightI.b / 252f));
            });
        return colorBuffer[i];
    }

    public void addToUpdateBuffer(String name, int id, int r, int g, int b) {
        Light light = identifiableColorBuffer.get(name)[id];

        r = Math.max(Math.min(r, 252), 0);
        g = Math.max(Math.min(g, 252), 0);
        b = Math.max(Math.min(b, 252), 0);

        if (light.r != r || light.g != g || light.b != b) {
            light.r = r;
            light.g = g;
            light.b = b;
            if (!colorBuffer[id].requiresUpdate) {
                lightsToUpdate.add(id);
                colorBuffer[id].requiresUpdate = true;
            }
        }
    }

    public Light[] getColorBuffer(){
        return colorBuffer;
    }
}
