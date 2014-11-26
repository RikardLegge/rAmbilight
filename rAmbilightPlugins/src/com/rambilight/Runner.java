package com.rambilight;

import com.rambilight.core.api.ui.Debugger;
import com.rambilight.plugins.Ambilight.Ambilight;

public class Runner {
    public static void main(String[] args) throws Exception {
        new Debugger(new Ambilight());
    }
}
