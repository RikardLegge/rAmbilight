package com.rambilight.plugins.Built_In_Effects;

import java.awt.CheckboxMenuItem;
import java.awt.Menu;
import java.awt.MenuItem;

import com.rambilight.core.Global;
import com.rambilight.core.ui.TrayController;
import com.rambilight.core.ui.TrayController.CustomCreator;
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

    public CustomCreator getTrayCreator() {
        return () -> {
            MenuItem[] items = new MenuItem[1];
            CheckboxMenuItem[] colorItems = new CheckboxMenuItem[effects.length];
            for (int i = 0; i < effects.length; i++) {
                final int id = i;
                colorItems[i] = TrayController.createCheckbox(effects[i] + "", id == currentEffect, (e) -> {
                    CheckboxMenuItem item = (CheckboxMenuItem) e.getSource();

                    for (CheckboxMenuItem mItem : colorItems)
                        if (!mItem.getLabel().equalsIgnoreCase(item.getLabel()))
                            mItem.setState(false);

                    index = 0;

                    for (int j = 0; j < Global.numLights; j++)
                        lightHandler.addToUpdateBuffer(j, 0, 0, 0);

                    currentEffect = id;
                    ((Menu) item.getParent()).setLabel("Effect (" + effects[currentEffect] + ")");
                });
            }
            items[0] = TrayController.createRadioGroup("Effect (" + effects[currentEffect] + ")", colorItems, (e) -> {
            });

            return items;
        };
    }

    public void loadPreferences() {
        currentEffect = preferences.load("currentEffect", currentEffect) > effects.length - 1 ? currentEffect : preferences.load("currentEffect", currentEffect);
    }

    public void savePreferences() {
        preferences.save("currentEffect", currentEffect);
    }

}