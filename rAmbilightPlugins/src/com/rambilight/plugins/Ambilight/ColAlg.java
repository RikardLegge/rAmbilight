package com.rambilight.plugins.Ambilight;

import java.awt.image.BufferedImage;

/**
 * Class that holds all parts of my color Algorithms
 */
public class ColAlg {

    public static int pixelIteration   = 10;
    public static int pixelMinFadeStep = 1;

    public static void pow(float div, float mult, float pow, int[] rgb) {
        for (int i = 0; i < rgb.length; i++)
            rgb[i] = Math.round(((float) Math.pow((float) rgb[i] / div, pow)) * mult);
    }

    public static void normalize(int[] rgb) {
        int strong = getStrongest(rgb);
        int normal = Math.max(1, rgb[strong] / 255);
        forEachValue(rgb, (i) -> {
            rgb[i] *= normal;
        });
    }

    public static void limit(int limit, int[] rgb) {
        for (int i = 0; i < rgb.length; i++)
            rgb[i] = Math.max(Math.min(rgb[i], limit), 0);
    }

    public static int diff(int colorA, int colorB) {
        return Math.abs(colorA - colorB);
    }

    public static void avg(int itt, int[] rgb) {
        if (itt == 0)
            return;
        for (int i = 0; i < rgb.length; i++)
            rgb[i] /= itt;
    }

    public static int getStrongest(int[] rgb) {
        int strongest = 0;
        int index = 0;
        for (int i = 0; i < rgb.length; i++)
            if (rgb[i] > strongest) {
                strongest = rgb[i];
                index = i;
            }
        return index;
    }

    public static int getTotal(int[] rgb) {
        return rgb[0] + rgb[1] + rgb[2];
    }

    public static int forEachPixel(int px, int py, int pw, int ph, int xdir, int ydir, BufferedImage image, CAFunc func) {
        int itt = 0;

        int xStep = pw / pixelIteration + 1;
        int yStep = ph / pixelIteration + 1;

        int xdirComp = xdir != -1 ? 1 : xdir;
        int ydirComp = ydir != -1 ? 1 : ydir;

        int ix = -pw / 2 + 1;
        int itterx = 0;
        while (ix < pw / 2) {
            int iy = -ph / 2 + 1;
            int ittery = 0;
            while (iy < ph / 2) {
                func.Each(image.getRGB(px + (ix) * xdirComp, py + (iy) * ydirComp));
                itt++;

                ittery++;
                iy += ydir == 0 ? yStep : ittery / 6 + pixelMinFadeStep;
            }
            itterx++;
            ix += xdir == 0 ? xStep : itterx / 6 + pixelMinFadeStep;

        }
        return itt;
    }

    public static int[] getAvgColor(BufferedImage image, int side, int numLights, int cr, int stepX, int stepY) {
        int[] avg = new int[]{0, 0, 0};
        int itt = 0;

        int h = stepY * numLights;
        int w = stepX * numLights;

        for (int i = 0; i < numLights; i++) {
            switch (side) {
                case 0: // Right
                    itt += forEachPixel(cr, h - i * stepY - stepY / 2, cr * 2, stepY, -1, 0, image, (pixel) -> ColAlg.add(pixel, avg));
                    break;
                case 1: // Top
                    itt += forEachPixel(w - i * stepX - stepX / 2, cr, stepX, cr * 2, 0, 1, image, (pixel) -> ColAlg.add(pixel, avg));
                    break;
                case 2: // Left
                    itt += forEachPixel(cr, i * stepY + stepY / 2, cr * 2, stepY, 1, 0, image, (pixel) -> ColAlg.add(pixel, avg));
                    break;
                case 3: // Bottom
                    itt += forEachPixel(i * stepX + stepX / 2, cr, stepX, cr * 2, 0, -1, image, (pixel) -> ColAlg.add(pixel, avg));
                    break;
            }
        }
        final int ittTot = itt;
        forEachValue(avg, (v) ->
        {
            avg[v] /= ittTot;
        });

        return avg;
    }

    public static int getBrightness(int[] rgb) {
        int bright = 0;
        for (int aRgb : rgb) bright += aRgb;
        return bright / rgb.length;
    }

    public static void forEachValue(int[] rgb, CAFunc func) {
        for (int i = 0; i < rgb.length; i++)
            func.Each(i);
    }

    public static void add(int pixel, int[] rgb) {
        rgb[0] += (255 & (pixel >> 16));
        rgb[1] += (255 & (pixel >> 8));
        rgb[2] += (255 & (pixel));
    }

    public static void set(int pixel, int[] rgb) {
        rgb[0] = (255 & (pixel >> 16));
        rgb[1] = (255 & (pixel >> 8));
        rgb[2] = (255 & (pixel));
    }

    public interface CAFunc {

        public void Each(int pixel);
    }
}