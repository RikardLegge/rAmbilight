package com.rambilight.plugins.Ambilight.extensions;

import com.rambilight.plugins.Ambilight.ColorAlgorithm;

import java.awt.image.BufferedImage;

public class SingleColor extends ColorAlgorithm {

    public String getName() {
        return "Single Color";
    }

    public void calculate(int px, int py, int pw, int ph, int xDir, int yDir, int side, BufferedImage image, int[] avg, int[] rgb) {
        int brightness = getBrightness(avg);
        rgb[0] = avg[0];
        rgb[1] = avg[1];
        rgb[2] = avg[2];
        pow(brightness, brightness, 2f, rgb);
        normalize(rgb);
    }
}
