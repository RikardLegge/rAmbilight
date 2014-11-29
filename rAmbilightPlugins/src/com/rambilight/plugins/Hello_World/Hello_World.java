package com.rambilight.plugins.Hello_World;

import com.rambilight.core.api.ui.TrayController;
import com.rambilight.plugins.Module;
import com.rambilight.plugins.extensions.Extension;

import java.awt.*;

public class Hello_World extends Module {

    // A recommended but not require variable. This helps to keep the frame rate at a stable level, since the step function can a lot longer than 10ms, depending on  the time other modules use.
    long lastStep = 0;

    // The current position of out white box.
    int currentPosition = 0;

    // A function which is called about every 10ms.
    public void step() {
        // Check if 100ms has passed since last time we did our update. If not, return without doing anything.
        if (System.currentTimeMillis() - lastStep < 100)
            return;
        // Update "lastStep" so that it corresponds to when the last update actually happened.
        lastStep = System.currentTimeMillis();

        // Sets the old "currentPosition" to black.
        lightHandler.addToUpdateBuffer(currentPosition, 0, 0, 0);

        // Moves the "currentPosition" forward one step.
        // If it reaches a position which isn't covered by lights, go back to 0 and start over again.
        currentPosition = (++currentPosition) % lightHandler.numLights();

        // At our new position, set the lights to be active
        lightHandler.addToUpdateBuffer(currentPosition, 255, 255, 255);
    }

    // To define a user interface in the menu bar, define it in here.
    public TrayController.CustomCreator getTrayCreator() {
        // Return a function which creates the part of the menu bar which we want to control our Module.
        return () -> {
            // Create a new MenuItem named "Reset position" and attach a function to it.
            // In the attached function, reset the light and set the "currentPosition" to 0.
            MenuItem resetPosition = TrayController.createItem("Reset Position", (target) -> {
                lightHandler.addToUpdateBuffer(currentPosition, 0, 0, 0);
                currentPosition = 0;
            });

            // Return a list with our menu items so that it can be created by the TrayController
            return new MenuItem[]{resetPosition};
        };
    }

    // Most often called on application LAUNCH or when the module is loaded into memory
    public void loadPreferences() {
        // Load the current position if it was saved.
        currentPosition = preferences.load("currentEffect", currentPosition);
    }

    // Most often called on application EXIT or when the module is unloaded from memory
    public void savePreferences() {
        // Save the current position so we can come back to where we left of last time.
        preferences.save("currentPosition", currentPosition);
    }

    // A simple way of loading extensions for this module.
    // Just drag and drop the extension into the plugins folder and make sure it follows the naming scheme:
    // MODULENAME.EXTENSIONNAME{.class|.jar}
    public void loadExtension(Class<Extension> extension) {
        System.out.println("Loading extension: " + extension.getSimpleName() + " ... ");
        try {
            extension.newInstance();
            System.out.println("SUCCESSFUL!");
        } catch (Exception e) {
            System.err.println("FAILED!");
        }
    }

}
