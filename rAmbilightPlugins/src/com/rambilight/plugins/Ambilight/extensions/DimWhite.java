package com.rambilight.plugins.Ambilight.extensions;

import com.rambilight.plugins.Ambilight.ColorAlgorithm;

import java.awt.image.BufferedImage;

public class DimWhite extends ColorAlgorithm {

    public String getName() {
        return "Dim White";
    }

    public void calculate(int px, int py, int pw, int ph, int xDir, int yDir, int side, BufferedImage image, int[] avg, int[] rgb) {
        int itt = forEachPixel(px, py, pw, ph, xDir, yDir, image, (pixel) -> add(pixel, rgb));
        int brightness = getBrightness(avg);
        avg(itt, rgb);
        pow(brightness, brightness, 2f, rgb);

        normalize(rgb);

        int diff = 0;
        for (int i = 0; i < 3; i++) {
            int tmpDiff = diff(rgb[i], rgb[(i + 1) % 3]);
            diff = diff > tmpDiff ? diff : tmpDiff;
        }

        if (diff <= 40)
            for (int i = 0; i < 3; i++)
                rgb[i] *= Math.pow((diff / 3f + 26f) / 40f, 2f);
    }
}
