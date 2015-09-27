package com.rambilight;

import com.rambilight.core.rAmbilight;
import com.rambilight.plugins.Ambilight.Ambilight;

public class Runner {
    public static void main(String[] args) throws Exception {
        new rAmbilight().loadDebugger(Ambilight.class);
    }
}
