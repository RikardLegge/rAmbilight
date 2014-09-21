package com.rambilight.dev;

import javax.swing.UIManager;

import com.rambilight.core.preferences.Global;
import com.rambilight.core.preferences.Preferences;
import com.rambilight.core.serial.LightHandler;
import com.rambilight.plugins.Module;

public class Debugger {

    private Visulizer visulizer;
    private TrayController   trayController;

    public Debugger(Module module) throws Exception {
        
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {}
        
        visulizer = new Visulizer(59);
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
