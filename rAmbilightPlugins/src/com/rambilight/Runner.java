package com.rambilight;

import com.rambilight.core.api.ui.Debugger;
import com.rambilight.plugins.Ambilight.Ambilight;
import com.rambilight.plugins.Built_In_Effects.Built_In_Effects;
import com.rambilight.plugins.PushBullet.PushBullet;

public class Runner {
    public static void main(String[] args) throws Exception {
        new Debugger(new Ambilight());
    }
}
