package com.rambilight.plugins.Built_In_Effects;

import java.awt.CheckboxMenuItem;
import java.awt.Menu;
import java.awt.MenuItem;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import com.rambilight.core.api.ui.TrayController;
import com.rambilight.plugins.Built_In_Effects.extensions.CirclingLights;
import com.rambilight.plugins.Built_In_Effects.extensions.Flashing;
import com.rambilight.plugins.Built_In_Effects.extensions.KnightRider;
import com.rambilight.plugins.Module;
import com.rambilight.plugins.extensions.Extension;

public class Built_In_Effects extends Module {

    private long lastStep = 0;
    private Effect currentEffect;
    private List<Effect> effects = new ArrayList<>();

    public Built_In_Effects() {
        addEffect(new CirclingLights());
        addEffect(new KnightRider());
        addEffect(new Flashing());
    }

    public void step() {
        if (System.currentTimeMillis() - lastStep < 50)
            return;
        lastStep = System.currentTimeMillis();
        currentEffect.step(lightHandler);
    }

    public TrayController.CustomCreator getTrayCreator() {
        return () -> {

            CheckboxMenuItem[] colorItems = new CheckboxMenuItem[effects.size()];
            System.out.println(effects.size());
            for (int i = 0; i < effects.size(); i++)
                colorItems[i] = TrayController.createCheckbox(effects.get(i).getName() + "", effects.get(i).equals(currentEffect), null);
            Menu effsel = TrayController.createRadioGroup("Effect (" + currentEffect.getName() + ")", colorItems, (target, i, parent) -> {
                for (int j = 0; j < lightHandler.numLights(); j++)
                    lightHandler.addToUpdateBuffer(j, 0, 0, 0);
                parent.setLabel("Effect (" + (currentEffect = effects.get(i)).getName() + ")");
            });

            return new MenuItem[]{effsel};
        };
    }

    public void loadPreferences() {
        String currentEffectName = preferences.load("currentEffect", effects.get(0).getName());
        for (Effect effect : effects)
            if (effect.getName().equals(currentEffectName)) {
                currentEffect = effect;
                break;
            }
        if (currentEffect == null)
            currentEffect = effects.get(0);

        for (Effect effect : effects)
            effect.loadPreferences(preferences);
    }

    public void savePreferences() {
        preferences.save("currentEffect", currentEffect.getName());

        for (Effect effect : effects)
            effect.savePreferences(preferences);
    }

    public void loadExtension(Class<Extension> extension) {
        try {
            Effect effect = (Effect) (extension).newInstance();
            if (addEffect(effect))
                System.out.println("Successfully loaded Effect: " + effect.getName());
        } catch (Exception e) {
            System.err.println("ERROR(Built In Effects):Failed to load extension of name " + extension.getSimpleName());
        }
    }

    private boolean addEffect(Effect effect) {
        if (!effects.contains(effect)) {
            effects.add(effect);
            return true;
        }
        else
            return false;
    }
}