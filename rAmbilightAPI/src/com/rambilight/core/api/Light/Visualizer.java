package com.rambilight.core.api.Light;

import com.rambilight.core.api.Global;

import javax.swing.*;
import java.awt.*;
import java.nio.file.AccessDeniedException;

/*
 * Original source
 * http://stackoverflow.com/questions/1190168/pass-mouse-events-to-applications-behind-from-a-java-ui
 * 
 * Edited to suite my needs. Credit to the original author
 */

/**
 * WARNING: The visualizer is not available outside of the API environment.
 * Therefor, don't call it from within a module, since this would cause it to be invalidated!
 */
public class Visualizer extends JFrame {

    private Rectangle screen = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
    private Light[] colorBuffer;

    public Visualizer(Class<?> Authentication, SetCallback setCallback) throws AccessDeniedException {

        if (!Authentication.getName().equals("com.rambilight.core.api.ui.Debugger"))
            throw new AccessDeniedException("Unable to initialize, since the expected authentication wasn't correct.");
        setCallback.set(() -> {
            update();
        });
        colorBuffer = new Light[Global.numLights];
        for (int i = 0; i < Global.numLights; i++)
            colorBuffer[i] = new Light(i, 0, 0, 0);

        setTitle("rAmbiligt Visualizer");
        try {
            setIconImage(new ImageIcon(Visualizer.class.getResource("Tray_Active.png")).getImage());
        } catch (Exception e) {
        }

        setDefaultLookAndFeelDecorated(true);
        //setAlwaysOnTop(true);
        setUndecorated(true);
        Component c = new JPanel() {

            @Override
            public void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(Color.black);
                int w = getWidth();
                int h = getHeight();
                g2.fillRect(0, 0, w, h);
                drawOutline(g2);
                g2.setComposite(AlphaComposite.Clear);
                g2.fillRect(10, 10, w - 20, h - 20);
            }
        };
        c.setPreferredSize(screen.getSize());
        getContentPane().add(c);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        pack();
        setVisible(true);
        com.sun.awt.AWTUtilities.setWindowOpaque(this, false);
    }

    protected void update() {
        repaint();
    }

    private int numLightsOnSide(int side) {
        return Global.lightLayout[side];// / Global.compressionLevel;
    }

    private void drawOutline(Graphics2D g) {
        int a = 10;
        int h = getHeight() - 2 * a;
        int w = getWidth() - 2 * a;
        int light = 0;
        int numSides = Global.lightLayout.length;

        if (numSides > 0) {
            float fhd = h / numLightsOnSide(0);
            int hd = Math.round(fhd);
            for (int i = 0; i < numLightsOnSide(0); i++) {
                g.setColor(getColor(light));
                g.fillRect(w + a, h - Math.round(fhd * i) - 2 * a, a, hd);
                light++;
            }
        }
        if (numSides > 1) {
            int fwd = w / numLightsOnSide(1);
            int wd = Math.round(fwd);
            for (int i = 0; i < numLightsOnSide(1); i++) {
                g.setColor(getColor(light));
                g.fillRect(w - Math.round(fwd * i) - a, 0, wd, a);
                light++;
            }
        }
        if (numSides > 2) {
            float fhd = h / numLightsOnSide(2);
            int hd = Math.round(fhd);
            for (int i = 0; i < numLightsOnSide(2); i++) {
                g.setColor(getColor(light));
                g.fillRect(0, Math.round(fhd * i) + a, a, hd);
                light++;
            }
        }
        if (numSides > 3) {
            int fwd = w / numLightsOnSide(3);
            int wd = Math.round(fwd);
            for (int i = 0; i < numLightsOnSide(3); i++) {
                g.setColor(getColor(light));
                g.fillRect(Math.round(fwd * i) + a, h + a, wd, a);
                light++;
            }
        }
    }

    protected void setColor(int l, int r, int g, int b) {
        if (l >= 0 && l <= Global.numLights) {
            colorBuffer[l].r = Math.max(Math.min(r, 252), 0);
            colorBuffer[l].g = Math.max(Math.min(g, 252), 0);
            colorBuffer[l].b = Math.max(Math.min(b, 252), 0);
        }
    }

    protected Light getLight(int light) {
        return colorBuffer[light];
    }

    protected Light[] getColorBuffer() {
        return colorBuffer;
    }

    protected Color getColor(int l) {
        if (colorBuffer[l] == null)
            return Color.black;
        else
            return new Color(colorBuffer[l].r, colorBuffer[l].g, colorBuffer[l].b);
    }

    public interface SetCallback {
        void set(Callback callback);
    }

    public interface Callback {
        void call();
    }
}