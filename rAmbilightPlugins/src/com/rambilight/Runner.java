package com.rambilight;

import com.rambilight.core.Main;
import com.rambilight.plugins.Ambilight.Ambilight;
import com.rambilight.plugins.Built_In_Effects.Built_In_Effects;

public class Runner {
    public static void main(String[] args) throws Exception {
        new Main().loadDebugger(Built_In_Effects.class);
    }
}
