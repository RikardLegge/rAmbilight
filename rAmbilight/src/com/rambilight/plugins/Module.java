package com.rambilight.plugins;

import com.legge.preferences.Preferences;
import com.rambilight.core.api.Light.LightHandler;
import com.rambilight.core.api.ui.TrayController.CustomCreator;

public abstract class Module {

    public LightHandler lightHandler;
    public Preferences  preferences;

    public void loaded() {
    }

    public void resume() {
    }

    public void step() {
    }

    public void suspend() {
    }

    public void dispose() {
    }

    public void savePreferences() {
    }

    public void loadPreferences() {
    }

    public abstract CustomCreator getTrayCreator();

}
