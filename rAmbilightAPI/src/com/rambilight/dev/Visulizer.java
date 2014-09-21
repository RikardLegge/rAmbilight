package com.rambilight.dev;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;

import javax.swing.JFrame;
import javax.swing.JPanel;

/*
 * Original source
 * http://stackoverflow.com/questions/1190168/pass-mouse-events-to-applications-behind-from-a-java-ui
 * 
 * Only edited to suite my needs. Full credit to the original author
 */

/** Frame controller for selecting the area of the ambilight screen capture */
@SuppressWarnings("serial") public class Visulizer extends JFrame {

    private Rectangle       screen = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
    private int[][] alrgb;

    protected Visulizer(int numLights) {
        alrgb = new int[numLights][];

        setDefaultLookAndFeelDecorated(true);
        setAlwaysOnTop(true);
        setUndecorated(true);
        Component c = new JPanel() {

            @Override public void paintComponent(Graphics g) {
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
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setVisible(true);
        com.sun.awt.AWTUtilities.setWindowOpaque(this, false);
    }

    private void drawOutline(Graphics2D g) {
        int a = 10;
        int h = getHeight();
        int w = getWidth();
        int hd = h / 15;
        int wd = w / 29;
        int light = 0;

        for (int i = 0; i < 15; i++) {
            g.setColor(getColor(light));
            g.fillRect(w - a, h - hd * (i+1), a, hd);
            light++;
        }
        for (int i = 0; i < 29; i++) {
            g.setColor(getColor(light));
            g.fillRect(w - wd * (i+1), 0, wd, a);
            light++;
        }
        for (int i = 0; i < 15; i++) {
            g.setColor(getColor(light));
            g.fillRect(0, hd * i, a, hd);
            light++;
        }
    }

    public void setColor(int l, int r, int g, int b) {
        if (l >= 0 && l <= 59) {
            if (alrgb[l] == null)
                alrgb[l] = new int[3];
            alrgb[l][0] = Math.max(Math.min(r, 252), 0);
            alrgb[l][1] = Math.max(Math.min(g, 252), 0);
            alrgb[l][2] = Math.max(Math.min(b, 252), 0);
        }
    }

    protected void update() {
        repaint();
    }

    private Color getColor(int l) {
        if (alrgb[l] == null)
            return Color.black;
        else
            return new Color(alrgb[l][0], alrgb[l][1], alrgb[l][2]);
    }
}