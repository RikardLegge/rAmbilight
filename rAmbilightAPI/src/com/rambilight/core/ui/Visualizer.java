package com.rambilight.core.ui;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;

import javax.swing.*;

import com.rambilight.core.Global;
import com.rambilight.core.serial.Light;

/*
 * Original source
 * http://stackoverflow.com/questions/1190168/pass-mouse-events-to-applications-behind-from-a-java-ui
 * 
 * Edited to suite my needs. Credit to the original author
 */

/**
 * Frame controller for selecting the area of the ambilight screen capture
 */
@SuppressWarnings("serial")
public class Visualizer extends JFrame {

    private Rectangle screen = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
    private Light[] colorBuffer;

    /**
     * The visualizer is not available outside of the API environment.
     * Therefor, don't call it from a module!
     */
    @Deprecated
    public Visualizer() {
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

    private void drawOutline(Graphics2D g) {
        int a = 10;
        int h = getHeight();
        int w = getWidth();
        int hd = h / 15;
        int wd = w / 30;
        int light = 0;


        if (Global.lightLayout.length > 0)
            for (int i = 0; i < Global.lightLayout[0]; i++) {
                g.setColor(getColor(light));
                g.fillRect(w - a, h - hd * (i + 1), a, hd);
                light++;
            }
        if (Global.lightLayout.length > 1)
            for (int i = 0; i < Global.lightLayout[1]; i++) {
                g.setColor(getColor(light));
                g.fillRect(w - wd * (i + 1), 0, wd, a);
                light++;
            }
        if (Global.lightLayout.length > 2)
            for (int i = 0; i < Global.lightLayout[2]; i++) {
                g.setColor(getColor(light));
                g.fillRect(0, hd * i, a, hd);
                light++;
            }
        if (Global.lightLayout.length > 3)
            for (int i = 0; i < Global.lightLayout[3]; i++) {
                g.setColor(getColor(light));
                g.fillRect(wd * i, h - a, wd, a);
                light++;
            }

    }

    public void setColor(int l, int r, int g, int b) {
        if (l >= 0 && l <= Global.numLights) {
            colorBuffer[l].r = Math.max(Math.min(r, 252), 0);
            colorBuffer[l].g = Math.max(Math.min(g, 252), 0);
            colorBuffer[l].b = Math.max(Math.min(b, 252), 0);
        }
    }

    public Light getLight(int light) {
        return colorBuffer[light];
    }

    public Light[] getColorBuffer() {
        return colorBuffer;
    }

    public Color getColor(int l) {
        if (colorBuffer[l] == null)
            return Color.black;
        else
            return new Color(colorBuffer[l].r, colorBuffer[l].g, colorBuffer[l].b);
    }
}