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

        for (int i = 0; i < 3; i++) {
            float x = rgb[i];
            float a = avg[i];
//            rgb[i] = (int) (-Math.pow(2f, (a - x + 357f) / 70f) + a + 34f); // $f_2=-2^{\frac{a-x+417}{70}}+a+24$
            rgb[i] = (int) (Math.pow((x - a - 110f) / 40f, 3f) + Math.pow((x - 40) / 40, 2f) + a);  // $f_3=\frac{\left(x-a-110\right)}{40}^3+\frac{\left(x-40\right)}{40}^2+a$

//            rgb[i] = 5*(int)(Math.pow(x/(59-a/28), 3) + Math.pow(x/(28-a/28), 2));
//            rgb[i] = (int) ((Math.pow((x - 120f) / 263f, 2f) * (x - 120f) + 25f) * 10f);

            rgb[i] = rgb[i] < 0 ? 0 : rgb[i];
            rgb[i] = rgb[i] > 255 ? 255 : rgb[i];
        }

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

    }

    public void loadPreferences(Preferences preferences) {

    }
}
