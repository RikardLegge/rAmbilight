package com.rambilight.plugins.Built_In_Effects;

import java.awt.CheckboxMenuItem;
import java.awt.Menu;
import java.awt.MenuItem;

import com.rambilight.core.preferences.Global;
import com.rambilight.core.ui.TrayController;
import com.rambilight.core.ui.TrayController.CustomCreator;
import com.rambilight.plugins.Module;

public class Built_In_Effects extends Module {

    private int     currentEffect = 0;

    private int     updateDelay   = 50;
    private int     index         = 0;
    private boolean istrue        = true;
    private long    lastStep      = 0;

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
            for (int i = 0; i < Global.numLights; i++)
                lightHandler.addToUpdateBuffer(i, (index), (index), (index));
            index = (index % (250 - 1)) + 15;
        break;
        case 2:

            if (index > 29 + 13) {
                istrue = false;
                index = 34+3;
            } else if (index < 16) {
                istrue = true;
                index = 24-3;
            }

            if (istrue) {
                index++;
                lightHandler.addToUpdateBuffer(index - 10, 0, 0, 0);
                lightHandler.addToUpdateBuffer(index, 250, 0, 0);
            } else {
                index--;
                lightHandler.addToUpdateBuffer(index + 10, 0, 0, 0);
                lightHandler.addToUpdateBuffer(index, 250, 0, 0);
            }
        break;

        default:
        break;
        }
    }

    public CustomCreator getTrayCreator() {
        return () -> {
            MenuItem[] items = new MenuItem[1];
            int[] effects = new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
            CheckboxMenuItem[] colorItems = new CheckboxMenuItem[effects.length];
            for (int i = 0; i < effects.length; i++)
                colorItems[i] = TrayController.createCheckbox(effects[i] + "", effects[i] == currentEffect, (e) -> {
                    CheckboxMenuItem item = (CheckboxMenuItem) e.getSource();

                    for (CheckboxMenuItem mItem : colorItems)
                        if (!mItem.getLabel().equalsIgnoreCase(item.getLabel()))
                            mItem.setState(false);

                    index = 0;

                    for (int j = 0; j < Global.numLights; j++)
                        lightHandler.addToUpdateBuffer(j, 0, 0, 0);

                    currentEffect = Integer.valueOf(item.getLabel());
                    ((Menu) item.getParent()).setLabel("Effect (" + currentEffect + ")");
                });
            items[0] = TrayController.createRadioGroup("Effect (" + currentEffect + ")", colorItems, (e) -> {});

            return items;
        };
    }

    public void loadPreferences() {
        currentEffect = preferences.load("currentEffect", currentEffect);
    }

    public void savePreferences() {
        preferences.save("currentEffect", currentEffect);
    }

}