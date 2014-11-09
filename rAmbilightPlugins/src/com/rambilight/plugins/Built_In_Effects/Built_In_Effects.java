package com.rambilight.plugins.Built_In_Effects;

import java.awt.CheckboxMenuItem;
import java.awt.Menu;
import java.awt.MenuItem;

import com.rambilight.core.api.Global;
import com.rambilight.core.api.ui.TrayController;
import com.rambilight.plugins.Module;

public class Built_In_Effects extends Module {

    private int currentEffect = 0;

    private int     updateDelay = 50;
    private int     index       = 0;
    private boolean istrue      = true;
    private long    lastStep    = 0;

    private static final String[] effects = new String[]{"Circling light", "Knight rider", "Stress test"};

    public void step() {
        if (System.currentTimeMillis() - lastStep < updateDelay)
            return;
        lastStep = System.currentTimeMillis();

        switch (currentEffect) {
            case 0:
                if (index - 4 < 0)
                    lightHandler.addToUpdateBuffer(Global.numLights - 4 + index, 0, 0, 0);
                else
                    lightHandler.addToUpdateBuffer(index - 4, 0, 0, 0);
                lightHandler.addToUpdateBuffer(index, 200, 200, 200);

                index = (index + 1) % Global.numLights;
                break;
            case 1:

                if (index > Global.lightLayout[0] + Global.lightLayout[1] - 2) {
                    istrue = false;
                    index = Global.lightLayout[0] + Global.lightLayout[1] - 5;
                }
                else if (index < Global.lightLayout[0] + 2) {
                    istrue = true;
                    index = Global.lightLayout[0] + 5;
                }

                if (istrue) {
                    index++;
                    lightHandler.addToUpdateBuffer(index - 10, 0, 0, 0);
                    lightHandler.addToUpdateBuffer(index, 250, 0, 0);
                }
                else {
                    index--;
                    lightHandler.addToUpdateBuffer(index + 10, 0, 0, 0);
                    lightHandler.addToUpdateBuffer(index, 250, 0, 0);
                }
                break;
            case 2:
                for (int i = 0; i < Global.numLights; i++)
                    lightHandler.addToUpdateBuffer(i, (index), (index), (index));
                index = (index % (250 - 1)) + 6;
                break;

            default:
                break;
        }
    }

    public TrayController.CustomCreator getTrayCreator() {
        return () -> {

            CheckboxMenuItem[] colorItems = new CheckboxMenuItem[effects.length];

            for (int i = 0; i < effects.length; i++)
                colorItems[i] = TrayController.createCheckbox(effects[i] + "", i == currentEffect, null);
            Menu effsel = TrayController.createRadioGroup("Effect (" + effects[currentEffect] + ")", colorItems, (target, i, parent) -> {
                for (int j = 0; j < Global.numLights; j++)
                    lightHandler.addToUpdateBuffer(j, 0, 0, 0);
                parent.setLabel("Effect (" + effects[currentEffect = i] + ")");
            });

            return new MenuItem[]{effsel};
        };
    }

    public void loadPreferences() {
        currentEffect = preferences.load("currentEffect", currentEffect) > effects.length - 1 ? currentEffect : preferences.load("currentEffect", currentEffect);
    }

    public void savePreferences() {
        preferences.save("currentEffect", currentEffect);
    }

}