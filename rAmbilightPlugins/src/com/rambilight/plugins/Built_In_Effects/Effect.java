package com.rambilight.plugins.Built_In_Effects;

import com.rambilight.core.api.Light.LightHandler;
import com.rambilight.core.preferences.Preferences;
import com.rambilight.plugins.extensions.Extension;

public abstract class Effect extends Extension {

    // Required extension functions
    public abstract String getName();

    public abstract void step(LightHandler lightHandler);

    // Optional extension functions
    public void savePreferences(Preferences preferences) {
    }

    public void loadPreferences(Preferences preferences) {
    }

}
