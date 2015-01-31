package com.rambilight.plugins.Ambilight.extensions;

import com.rambilight.core.preferences.Preferences;
import com.rambilight.plugins.Ambilight.ColorAlgorithm;

import javax.swing.tree.VariableHeightLayoutCache;
import java.awt.image.BufferedImage;

public class Default extends ColorAlgorithm {

    public String getName() {
        return "Default";
    }

    public void calculate(int px, int py, int pw, int ph, int xDir, int yDir, int side, BufferedImage image, int[] avg, int[] rgb) {
        // Get color related to the current area.
        float diffThreshHold = 8;
        int colorSuppress = 1;

        int itt = forEachPixel(px, py, pw, ph, xDir, yDir, image, (pixel) -> add(pixel, rgb));
        int brightness = getBrightness(avg);
        avg(itt, rgb);

//        // Blend it with the average.
//        rgb[0] = (avg[0] * colorSuppress + rgb[0]) / (colorSuppress + 1);
//        rgb[1] = (avg[1] * colorSuppress + rgb[1]) / (colorSuppress + 1);
//        rgb[2] = (avg[2] * colorSuppress + rgb[2]) / (colorSuppress + 1);

        for (int i = 0; i < 3; i++){
            rgb[i] = (int) (Math.pow((rgb[i] - avg[i] - 100f) / 40f, 3f) + Math.pow(rgb[i] / 40f, 2f) + avg[i]);
            rgb[i] = rgb[i] < 0 ? 0 : rgb[i];
            rgb[i] = rgb[i] > 255 ? 255 : rgb[i];
        }

        pow(10, 10, 2f, rgb);
        normalize(rgb);

        for (int i = 0; i < 3; i++) {
            float a = 10;
            float x = rgb[i];
            rgb[i] = (int) ((a * Math.pow(x, 2f)) / (255f + x * (a - 1f)));
        }

        // Get the maximum difference between the calculated color and the average.
        int diff = 0;
        for (int i = 0; i < 3; i++) {
            int tmpDiff = rgb[i] - avg[i];
            diff = diff > tmpDiff ? diff : tmpDiff;
        }

        // Dim the color to match the average.
//        if (diff > 0 && diff <= diffThreshHold)
//            for (int i = 0; i < 3; i++)
//                rgb[i] = avg[i] + (int) (Math.pow(diff, 2f) / diffThreshHold);
    }

    public void savePreferences(Preferences preferences) {

    }

    public void loadPreferences(Preferences preferences) {

    }
}
