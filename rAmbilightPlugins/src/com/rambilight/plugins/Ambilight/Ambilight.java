package com.rambilight.plugins.Ambilight;import java.awt.AWTException;import java.awt.CheckboxMenuItem;import java.awt.Color;import java.awt.Dimension;import java.awt.Menu;import java.awt.MenuItem;import java.awt.Point;import java.awt.Rectangle;import java.awt.Robot;import java.awt.event.ItemEvent;import java.awt.image.BufferedImage;import com.rambilight.core.preferences.Global;import com.rambilight.core.ui.TrayController;import com.rambilight.core.ui.TrayController.CustomCreator;import com.rambilight.plugins.Module;/** A controller that uses screencapture to create an Ambilight feeling on the specified computer */public class Ambilight extends Module {    private Robot     robot;    // Vaiables that control how screenshots are managed. Dynamic    private int       captureRadius       = 24;                             // capture radius of the screencapture & averaging.    private Rectangle captureArea         = new Rectangle(11, 11, 1898, 1068);    private boolean   simultaneousCapture = true;    private int       updateDelay         = 75;    private int       colorAlgorithm      = 8;    // Vaiables that control how screenshots are managed. Static    Rectangle[]       sides               = new Rectangle[0];    int               currentRectangle    = -1;    private long      lastStep            = 0;     public Ambilight() throws AWTException {        sides = new Rectangle[Global.lightLayout.length];        robot = new Robot();    }    public void loaded() {        setCaptureRectangle(captureArea);    }    public void step() {        if (System.currentTimeMillis() - lastStep < updateDelay)            return;        lastStep = System.currentTimeMillis();        if (simultaneousCapture)            fullCapture();        else            capture(currentRectangle = (currentRectangle + 1) % sides.length);    }    /** Called when the control resumes after being suspended */    public void resume() {        boolean cache = simultaneousCapture;        simultaneousCapture = true;        step();        simultaneousCapture = cache;    }    /** Called when the tray is loaded */    public CustomCreator getTrayCreator() {        return () -> getTrayController();    }    /** Generates as the specific tray items that this controller requires */    public MenuItem[] getTrayController() {        // Create the base tray handler        MenuItem[] items = new MenuItem[5];        // Add custom items for this specific Controller        items[0] = TrayController.createCheckbox("Simultaneous capture", simultaneousCapture, (e) -> {            simultaneousCapture = e.getStateChange() == ItemEvent.SELECTED;        });        // Add a toggleble delay value submenu        int[] delayValues = new int[] { 1000, 750, 500, 400, 300, 200, 150, 100, 75, 50, 25, 10 };        CheckboxMenuItem[] delayItems = new CheckboxMenuItem[delayValues.length];        for (int i = 0; i < delayValues.length; i++)            delayItems[i] = TrayController.createCheckbox(delayValues[i] + "", delayValues[i] == updateDelay, (e) -> {                CheckboxMenuItem item = (CheckboxMenuItem) e.getSource();                for (CheckboxMenuItem mItem : delayItems)                    if (!mItem.getLabel().equalsIgnoreCase(item.getLabel()))                        mItem.setState(false);                updateDelay = Integer.valueOf(item.getLabel());                ((Menu) item.getParent()).setLabel("Capture delay (" + updateDelay + "ms)");            });        items[1] = TrayController.createRadioGroup("Capture delay (" + updateDelay + "ms)", delayItems, (e) -> {});        items[2] = TrayController.createItem("Change capture rectangle", (e) -> {            new FrameController(captureArea.getLocation(), captureArea.getSize(), captureRadius * 2,                    (position, dimension, border) -> setCaptureRectangle(position, dimension, border / 2));        });        items[3] = TrayController.createItem("Reset capture rectangle", (e) -> {            captureArea.setLocation(0, 0);            captureArea.setSize(1920, 1080);            setCaptureRectangle(captureArea.getLocation(), captureArea.getSize(), 24);        });        int[] colorAlgoritms = new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };        CheckboxMenuItem[] colorItems = new CheckboxMenuItem[colorAlgoritms.length];        for (int i = 0; i < colorAlgoritms.length; i++)            colorItems[i] = TrayController.createCheckbox(colorAlgoritms[i] + "", colorAlgoritms[i] == colorAlgorithm, (e) -> {                CheckboxMenuItem item = (CheckboxMenuItem) e.getSource();                for (CheckboxMenuItem mItem : colorItems)                    if (!mItem.getLabel().equalsIgnoreCase(item.getLabel()))                        mItem.setState(false);                colorAlgorithm = Integer.valueOf(item.getLabel());                ((Menu) item.getParent()).setLabel("Color Algorithm (" + colorAlgorithm + ")");            });        items[4] = TrayController.createRadioGroup("Color Algorithm (" + colorAlgorithm + ")", colorItems, (e) -> {});        return items;    }    BufferedImage screenshot;    /** Capture a single side of the capture area */    private void capture(int side) {        screenshot = robot.createScreenCapture(sides[side]);        processCapture(side, screenshot);        screenshot.flush();    }    /** Capture all the side of the capture area at once */    private void fullCapture() {        for (int i = 0; i < sides.length; i++) {            capture(i);        }    }    /** Processes the captured image and adds the new pixel values to the lighthandler */    private void processCapture(int side, BufferedImage screenshot) {        /*int light = Global.numLights -1;        for (int i = 0; i < side; i++)            light -= Global.lightLayout[i];*/        int light = 0;        for (int i = 0; i < side; i++)            light += Global.lightLayout[i];        int[] rgb = new int[] { 0, 0, 0 };        int i = 0;        Rectangle rect = sides[side];        int stepX = (int) (rect.getHeight() / (Global.lightLayout[side] + 1));        int stepY = (int) (rect.getWidth() / (Global.lightLayout[side] + 1));        int[] avg = ColAlg.getAvgColor(screenshot, side, Global.lightLayout[side], captureRadius, stepX, stepY);        while (i < Global.lightLayout[side]) {            rgb[0] = 0;            rgb[1] = 0;            rgb[2] = 0;            switch (side) {            case 0: // Right                getAverage(captureRadius, (Global.lightLayout[side] - i) * stepX, screenshot, avg, rgb);            break;            case 1: // Top                getAverage((Global.lightLayout[side]-i) * stepY, captureRadius, screenshot, avg, rgb);            break;            case 2: // Left                getAverage(screenshot.getWidth() - captureRadius, (i + 1) * stepX, screenshot, avg, rgb);            break;            case 3: // Bottom                getAverage((i + 1) * stepY, screenshot.getHeight() - captureRadius, screenshot, avg, rgb);            break;            default:            break;            }            lightHandler.addToUpdateBuffer(light, rgb[0], rgb[1], rgb[2]);            light++;            i++;        }    }    /** The function that gets the average color for each light */    private void getAverage(int px, int py, BufferedImage image, int[] avg, int[] rgb) {        float[] hsv = new float[3];        int itt;        int brightness;        switch (colorAlgorithm) {        case 1:            itt = ColAlg.forEachPixel(px, py, image, captureRadius, (pixel) -> ColAlg.add(pixel, rgb));            ColAlg.avg(itt, rgb);            ColAlg.pow(127f, 255f, 2f, rgb);        break;        case 2:            itt = ColAlg.forEachPixel(px, py, image, captureRadius, (pixel) -> ColAlg.add(pixel, rgb));            ColAlg.avg(itt, rgb);            ColAlg.pow(220f, 600f, 4f, rgb);        break;        case 3:            itt = ColAlg.forEachPixel(px, py, image, captureRadius, (pixel) -> ColAlg.add(pixel, rgb));            ColAlg.avg(itt, rgb);            ColAlg.pow(220f, 400f, 3f, rgb);        break;        case 4:            itt = ColAlg.forEachPixel(px, py, image, captureRadius, (pixel) -> ColAlg.add(pixel, rgb));            ColAlg.avg(itt, rgb);            ColAlg.pow(300f, 400f, 3f, rgb);            ColAlg.limit(50, rgb);        break;        case 5:            itt = ColAlg.forEachPixel(px, py, image, captureRadius, (pixel) -> ColAlg.add(pixel, rgb));            ColAlg.avg(itt, rgb);            Color.RGBtoHSB(rgb[0], rgb[1], rgb[2], hsv);            hsv[2] = Math.min(hsv[2] * 2f, 1f);            ColAlg.set(Color.HSBtoRGB(hsv[0], hsv[1], hsv[2]), rgb);            ColAlg.pow(300f, 400f, 3f, rgb);        break;        case 6:            itt = ColAlg.forEachPixel(px, py, image, captureRadius, (pixel) -> ColAlg.add(pixel, rgb));            ColAlg.avg(itt, rgb);            rgb[0] *= .9;            rgb[1] *= 1.1;            rgb[2] *= 1.0;            ColAlg.normalize(rgb);        break;        case 7:            itt = ColAlg.forEachPixel(px, py, image, captureRadius, (pixel) -> ColAlg.add(pixel, rgb));            brightness = ColAlg.getBrightness(avg);            ColAlg.avg(itt, rgb);            ColAlg.pow(brightness, brightness, 2f, rgb);            ColAlg.normalize(rgb);        break;        case 8:            itt = ColAlg.forEachPixel(px, py, image, captureRadius, (pixel) -> ColAlg.add(pixel, rgb));            brightness = ColAlg.getBrightness(avg)+20;            ColAlg.avg(itt, rgb);            ColAlg.pow(brightness, brightness, 2f, rgb);            ColAlg.normalize(rgb);        break;        default:        break;        }        ColAlg.limit(252, rgb);    }    /** Sets the capture area of the screen capture */    private void setCaptureRectangle(Point p, Dimension d, int r) {        setCaptureRectangle(p.x, p.y, d.width, d.height, r);    }    /** Sets the capture area of the screen capture */    private void setCaptureRectangle(Rectangle a) {        setCaptureRectangle(a.x, a.y, a.width, a.height, captureRadius);    }    /** Sets the capture area of the screen capture */    private void setCaptureRectangle(int x, int y, int w, int h, int r) {        captureArea.setLocation(x, y);        captureArea.setSize(w, h);        captureRadius = r;        if (Global.lightLayout.length > 0)            sides[0] = new Rectangle(x + w - r * 2, 0, r * 2, h);   // Right        if (Global.lightLayout.length > 1)            sides[1] = new Rectangle(x, y, w, r * 2);               // Top        if (Global.lightLayout.length > 2)            sides[2] = new Rectangle(x, 0, r * 2, h);               // Left        if (Global.lightLayout.length > 3)            sides[3] = new Rectangle(x, y + h - r * 2, w, r * 2);   // Bottom    }    public void loadPreferences() {        captureRadius = preferences.load("captureRadius", captureRadius);        captureArea.x = preferences.load("CapturePosition.x", captureArea.x);        captureArea.y = preferences.load("CapturePosition.y", captureArea.y);        captureArea.width = preferences.load("CaptureDimension.width", captureArea.width);        captureArea.height = preferences.load("CaptureDimension.height", captureArea.height);        simultaneousCapture = preferences.load("simultaneousCapture", simultaneousCapture);        updateDelay = preferences.load("updateDelay", updateDelay);        colorAlgorithm = preferences.load("colorAlgorithm", colorAlgorithm);    }    public void savePreferences() {        preferences.save("captureRadius", captureRadius);        preferences.save("CapturePosition.x", captureArea.x);        preferences.save("CapturePosition.y", captureArea.y);        preferences.save("CaptureDimension.width", captureArea.width);        preferences.save("CaptureDimension.height", captureArea.height);        preferences.save("simultaneousCapture", simultaneousCapture);        preferences.save("updateDelay", updateDelay);        preferences.save("colorAlgorithm", colorAlgorithm);    }}/** Class that holds all parts of my color Algorithms */class ColAlg {    public static void pow(float div, float mult, float pow, int[] rgb) {        for (int i = 0; i < rgb.length; i++)            rgb[i] = Math.round(((float) Math.pow((float) rgb[i] / div, pow)) * mult);    }    public static void normalize(int[] rgb) {        int strong = getStrongest(rgb);        int normal = Math.max(1, rgb[strong] / 255);        forEachValue(rgb, (i) -> {            rgb[i] *= normal;        });    }    public static void limit(int limit, int[] rgb) {        for (int i = 0; i < rgb.length; i++)            rgb[i] = Math.max(Math.min(rgb[i], limit), 0);    }    public static void avg(int itt, int[] rgb) {        if (itt == 0)            return;        for (int i = 0; i < rgb.length; i++)            rgb[i] /= itt;    }    public static int getStrongest(int[] rgb) {        int strongest = 0;        int index = 0;        for (int i = 0; i < rgb.length; i++)            if (rgb[i] > strongest) {                strongest = rgb[i];                index = i;            }        return index;    }    public static int getTotal(int[] rgb) {        return rgb[0] + rgb[1] + rgb[2];    }    public static int forEachPixel(int px, int py, BufferedImage image, int captureRadius, int step, CAFunc func) {        int itt = 0;        int r = captureRadius - step;        for (int ix = -r; ix < r; ix += step) {            for (int iy = -r; iy < r; iy += step) {                try {                    func.Each(image.getRGB(px + ix, py + iy));                    itt++;                } catch (Exception e) {}            }        }        return itt;    }    public static int forEachPixel(int px, int py, BufferedImage image, int captureRadius, CAFunc func) {        return forEachPixel(px, py, image, captureRadius, Math.round(captureRadius / 3), func);    }    public static int[] getAvgColor(BufferedImage image, int side, int numLights, int captureRadius, int stepX, int stepY) {        int[] avg = new int[] { 0, 0, 0 };        int itt = 0;        for (int i = 0; i < numLights; i++)            switch (side) {            case 0: // Right                itt += forEachPixel(captureRadius, (numLights - i) * stepX, image, captureRadius, (pixel) -> ColAlg.add(pixel, avg));            break;            case 1: // Top                itt += forEachPixel((i + 1) * stepY, captureRadius, image, captureRadius, (pixel) -> ColAlg.add(pixel, avg));            break;            case 2: // Left                itt += forEachPixel(image.getWidth() - captureRadius, (i + 1) * stepX, image, captureRadius, (pixel) -> ColAlg.add(pixel, avg));            break;            case 3: // Bottom                itt += forEachPixel((i + 1) * stepY, image.getHeight() - captureRadius, image, captureRadius, (pixel) -> ColAlg.add(pixel, avg));            break;            }        final int ittTot = itt;        forEachValue(avg, (v) -> {            avg[v] /= ittTot;        });        return avg;    }    public static int getBrightness(int[] rgb) {        int bright = 0;        for (int i = 0; i < rgb.length; i++)            bright += rgb[0];        return bright / rgb.length;    }    public static void forEachValue(int[] rgb, CAFunc func) {        for (int i = 0; i < rgb.length; i++)            func.Each(i);    }    public static void add(int pixel, int[] rgb) {        rgb[0] += (255 & (pixel >> 16));        rgb[1] += (255 & (pixel >> 8));        rgb[2] += (255 & (pixel));    }    public static void set(int pixel, int[] rgb) {        rgb[0] = (255 & (pixel >> 16));        rgb[1] = (255 & (pixel >> 8));        rgb[2] = (255 & (pixel));    }    public interface CAFunc {        public void Each(int pixel);    }}