package com.rambilight.plugins;

import com.legge.preferences.Preferences;
import com.rambilight.core.api.Light.LightHandler;
import com.rambilight.core.api.ui.TrayController.CustomCreator;

public abstract class Module {

    /*
     * A pointer to the light handler which is connected to this module
     */
    public LightHandler lightHandler;

    /*
     * A pointer to the Preference handler which is connected to this module
     */
    public Preferences preferences;

    /*
     * Callback on initial load
     */
    public void loaded() {
    }

    /*
     * Callback when resumed from an idle state
     */
    public void resume() {
    }

    /*
     * Stepping function with a interval of 10ms
     */
    public void step() {
    }

    /*
     * Callback when suspended into an idle state
     */
    public void suspend() {
    }

    /*
     * Callback when disposed
     */
    public void dispose() {
    }

    /*
     * Callback when the preferences should be saved
     */
    public void savePreferences() {
    }

    /*
     * Callback when the preferences should be loaded
     */
    public void loadPreferences() {
    }

    /*
     * Function to create a custom part of the tray item list
     */
    public CustomCreator getTrayCreator() {
        return null;
    }

}
