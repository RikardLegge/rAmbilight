package com.rambilight.dev;

import javax.swing.UIManager;

import com.rambilight.core.Global;
import com.legge.preferences.Preferences;
import com.rambilight.core.serial.LightHandler;
import com.rambilight.plugins.Module;

public class Debugger {

    private Visulizer visulizer;
    private TrayController   trayController;

    public Debugger(Module module) throws Exception {
        
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Unable to set a custom look and feel.");
        }
        
        visulizer = new Visulizer();
        trayController = new TrayController();

        module.lightHandler = new LightHandler(visulizer);
        module.preferences = new Preferences("Built In Effets");
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
    }
}
