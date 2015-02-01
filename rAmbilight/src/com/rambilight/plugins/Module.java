package com.rambilight.plugins;

import com.rambilight.core.api.Light.LightHandler;
import com.rambilight.core.api.ui.TrayController.CustomCreator;
import com.rambilight.core.preferences.Preferences;
import com.rambilight.plugins.extensions.Extension;

/**
 * The Module class is the base for crating any new plugins for the application.
 * To use it, extend your plugin class from the Module class and override the functions which your
 * plugin requires.
 */
public abstract class Module {

    /**
     * A pointer to the light handler which is connected to this module
     */
    public LightHandler lightHandler;

    /**
     * A pointer to the Preference handler which is connected to this module
     */
    public Preferences preferences;

    /**
     * Callback on initial load
     *
     * @throws java.lang.Exception Throw an exception if the initialization failed to allow the module
     *                             loader to prevent it from continuing loading.
     */
    public void loaded() throws Exception {
    }

    /**
     * Callback when resumed from an idle state
     */
    public void resume() {
    }

    /**
     * Stepping function with a interval of 10ms
     */
    public void step() {
    }

    /**
     * Callback when suspended into an idle state
     */
    public void suspend() {
    }

    /**
     * Callback when disposed
     */
    public void dispose() {
    }

    /**
     * Callback when the preferences should be saved
     */
    public void savePreferences() {
    }

    /**
     * Callback when the preferences should be loaded
     */
    public void loadPreferences() {
    }

    /**
     * Callback when the extensions related to this module should be loaded.
     *
     * @param extension The class of the loaded extension.
     *                  It's up to the module to cast the class to the right type.
     */
    public void loadExtension(Class<Extension> extension) {
    }

    /**
     * Function to create a custom part of the tray item list
     *
     * @return Return a function which in turn can create the tray items which are related to this Module
     */
    public CustomCreator getTrayCreator() {
        return null;
    }

}
