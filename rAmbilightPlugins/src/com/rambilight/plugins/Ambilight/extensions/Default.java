package com.rambilight.plugins.Ambilight.extensions;

import com.rambilight.core.preferences.Preferences;
import com.rambilight.plugins.Ambilight.ColorAlgorithm;

import java.awt.image.BufferedImage;

public class Default extends ColorAlgorithm {
    private static int colorSuppress = 2;

    public String getName() {
        return "Default";
    }

    public void calculate(int px, int py, int pw, int ph, int xDir, int yDir, int side, BufferedImage image, int[] avg, int[] rgb) {
        // Get color related to the current area.
        int itt = forEachPixel(px, py, pw, ph, xDir, yDir, image, (pixel) -> add(pixel, rgb));
        int brightness = getBrightness(avg);
        avg(itt, rgb);

        // Blend it with the average.
        rgb[0] = (avg[0] * colorSuppress + rgb[0]) / (colorSuppress + 1);
        rgb[1] = (avg[1] * colorSuppress + rgb[1]) / (colorSuppress + 1);
        rgb[2] = (avg[2] * colorSuppress + rgb[2]) / (colorSuppress + 1);

        pow(brightness, brightness, 2f, rgb);
        normalize(rgb);

        // Get the maximum difference of the color channels.
        int diff = 0;
        for (int i = 0; i < 3; i++) {
            int tmpDiff = diff(rgb[i], rgb[(i + 1) % 3]);
            diff = diff > tmpDiff ? diff : tmpDiff;
            if (rgb[i] < 3)
                rgb[i] = 0;
        }

        // Dim the white channel.
        if (diff <= 20)
            for (int i = 0; i < 3; i++)
                rgb[i] *= Math.pow((diff / 3f + 13f) / 20f, 2f);
    }

    public void savePreferences(Preferences preferences) {
        preferences.save("colorAlgorithm.Default.colorSuppress", colorSuppress);
    }

    public void loadPreferences(Preferences preferences) {
        colorSuppress = preferences.load("colorAlgorithm.Default.colorSuppress", colorSuppress);
    }
}
