package com.rambilight.plugins.extensions;

import com.legge.preferences.Preferences;

/**
 * The Extension class is the base for crating any new extensions for rAmbilight Modules.
 * To use it, extend your extension from the Extension class and override the functions which your
 * plugin requires.
 */
public class Extension {

    /**
     * @return The name of the extension which the module should see.
     */
    public String getName() {
        return "Extension";
    }

    /**
     * Callback when the preferences should be saved
     *
     * @param preferences The preference connected connected to the parent Module
     */
    public void savePreferences(Preferences preferences) {
    }

    /**
     * Callback when the preferences should be loaded
     *
     * @param preferences The preference connected connected to the parent Module
     */
    public void loadPreferences(Preferences preferences) {
    }

}
