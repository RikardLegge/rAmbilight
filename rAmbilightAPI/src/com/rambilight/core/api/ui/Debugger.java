package com.rambilight.core.api.ui;

import com.legge.preferences.Preferences;
import com.rambilight.core.api.Global;
import com.rambilight.core.api.Light.LightHandler;
import com.rambilight.core.api.Light.Visualizer;
import com.rambilight.plugins.Module;

import javax.swing.*;

/**
 * WARNING: The debugger is not available outside of the API environment.
 * Therefor, don't call it from within a module, since this would cause it to be invalidated!
 */
public class Debugger {

    private Visualizer.Callback visualizerUpdate;

    public Debugger(Module module) throws Exception {

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Unable to set a custom look and feel.");
        }


        Visualizer.SetCallback getCallback = (callback) -> visualizerUpdate = callback;
        Visualizer visulizer = new Visualizer(this.getClass(), getCallback);
        TrayController trayController = new TrayController(module.getClass().getSimpleName());

        module.lightHandler = new LightHandler(visulizer);
        module.preferences = new Preferences(module.getClass().getSimpleName());
        module.loaded();
        trayController.addToTrayController(module.getTrayCreator());

        while (!Global.requestExit)
            if (Global.isActive)
                try {
                    module.step();
                    visualizerUpdate.call();
                    Thread.sleep(10);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            else
                try {
                    Thread.sleep(500);
                } catch (Exception e) {
                    e.printStackTrace();
                }

    }
}
