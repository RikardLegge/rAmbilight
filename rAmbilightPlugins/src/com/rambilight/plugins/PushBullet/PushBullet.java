package com.rambilight.plugins.PushBullet;

import java.awt.MenuItem;
import java.util.Hashtable;

import com.rambilight.core.Global;
import com.rambilight.core.ui.MessageBox;
import com.rambilight.core.ui.TrayController.CustomCreator;
import com.rambilight.plugins.Module;
import com.rambilight.plugins.PushBullet.PushBulletEndpoint.PushBulletEndpointListener;

public class PushBullet extends Module {

    String apiKey;
    Hashtable<String, String> colors       = new Hashtable<>();
    int[]                     currentColor = new int[]{0, 0, 0};
    int                       currentStage = -1;
    int[]                     animation    = new int[]{1, 0, 1, 0, 1};

    long lastStep = 0;

    public void loaded() {
        if (apiKey == null)
            apiKey = MessageBox.Input("Pushbullet", "Please enter your API key");

        PushBulletEndpoint.setAPiKey(apiKey);
        PushBulletEndpoint.setListener(new PushBulletEndpointListener() {

            public void onMessage(String s) {
                System.out.println(s);
                setCurrentColor(s);
                currentStage = 0;
            }

            public void onError(String s) {
                System.err.println(s);
            }
        });
        PushBulletEndpoint.open();
        currentColor[0] = 255 * animation[animation.length - 1];
        currentColor[1] = 255 * animation[animation.length - 1];
        currentColor[2] = 255 * animation[animation.length - 1];
        for (int i = 0; i < Global.numLights; i++)
            lightHandler.addToUpdateBuffer(i, currentColor[0], currentColor[1], currentColor[2]);
    }

    private void setCurrentColor(String applicationName) {
        if (!colors.containsKey("applicationName"))
            colors.put(applicationName, "#FFFFFF");
        String hex = colors.get(applicationName);
        currentColor[0] = Integer.valueOf(hex.substring(1, 3), 16);
        currentColor[1] = Integer.valueOf(hex.substring(3, 5), 16);
        currentColor[2] = Integer.valueOf(hex.substring(5, 7), 16);
    }

    public void step() {
        if (System.currentTimeMillis() - lastStep < 500)
            return;
        lastStep = System.currentTimeMillis();

        if (currentStage > -1) {
            int stre = animation[currentStage];
            for (int i = 0; i < Global.numLights; i++)
                lightHandler.addToUpdateBuffer(i, currentColor[0] * stre, currentColor[1] * stre, currentColor[2] * stre);
            currentStage++;
            if (currentStage > animation.length - 1)
                currentStage = -1;
        }
    }

    public CustomCreator getTrayCreator() {
        return () -> {
            return new MenuItem[0];
        };
    }

    public void loadPreferences() {
        animation = preferences.load("animation", animation, -1);
        apiKey = preferences.load("apiKey", null);

        String[] keys = preferences.load("configuredApplications", new String[0], -1);
        for (String key : keys)
            colors.put(key, preferences.load(key, "#FFFFFF"));
    }

    public void savePreferences() {

        preferences.save("animation", animation);
        preferences.save("apiKey", apiKey);

        int i = 0;
        String[] keys = new String[colors.size()];
        for (String key : colors.keySet()) {
            keys[i++] = key;
            preferences.save(key, colors.get(key));
        }
        if (keys.length > 0)
            preferences.save("configuredApplications", keys);
    }

}