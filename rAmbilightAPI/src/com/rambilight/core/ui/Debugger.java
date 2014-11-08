package com.rambilight.core.ui;

import com.legge.preferences.Preferences;
import com.rambilight.core.Global;
import com.rambilight.core.serial.LightHandler;
import com.rambilight.plugins.Module;

import javax.swing.*;

public class Debugger {

    public Debugger(Module module) throws Exception {

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Unable to set a custom look and feel.");
        }

        Visualizer visulizer = new Visualizer();
        TrayController trayController = new TrayController();

        module.lightHandler = new LightHandler(visulizer);
        module.preferences = new Preferences(module.getClass().getSimpleName());
        module.loaded();
        trayController.addToTrayController(module.getTrayCreator());

        while (!Global.requestExit)
            if (Global.isActive)
                try {
                    module.step();
                    visulizer.update();
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
