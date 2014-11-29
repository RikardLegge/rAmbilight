package com.rambilight.plugins.Ambilight.extensions;

import com.rambilight.plugins.Ambilight.ColorAlgorithm;

import java.awt.image.BufferedImage;

public class Classic extends ColorAlgorithm {

    public String getName() {
        return "Classic";
    }

    public void calculate(int px, int py, int pw, int ph, int xDir, int yDir, int side, BufferedImage image, int[] avg, int[] rgb) {
        int itt = forEachPixel(px, py, pw, ph, xDir, yDir, image, (pixel) -> add(pixel, rgb));
        int brightness = getBrightness(avg);
        avg(itt, rgb);

        pow(brightness, brightness, 2f, rgb);

        normalize(rgb);
    }
}
