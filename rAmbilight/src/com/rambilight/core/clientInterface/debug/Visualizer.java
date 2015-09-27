package com.rambilight.core.clientInterface.debug;

import com.rambilight.core.api.Global;
import com.rambilight.core.api.Side;

import javax.swing.*;
import java.awt.*;

/*
 * Original source
 * http://stackoverflow.com/questions/1190168/pass-mouse-events-to-applications-behind-from-a-java-ui
 * 
 * Edited to suite my needs. Credit to the original author
 */

/**
 * A way of visualizing the light buffer in the same way as the arduino and led strip would.
 * Creates a frame and interface for displaying the light buffer.
 */
public class Visualizer extends JFrame {

	private Rectangle screen = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
	ArduinoEmulator.rgb_color[] colorBuffer;
	int                         light;

	public Visualizer(ArduinoEmulator.rgb_color[] colorBuffer) {
		this.colorBuffer = colorBuffer;
		createUI();
	}

	public void update() {
		repaint();
	}

	private void createUI() {
		setTitle("rAmbiligt Visualizer");
		try {
			setIconImage(new ImageIcon(Visualizer.class.getResource("Tray_Active.png")).getImage());
		} catch (Exception e) {
		}

		Component c = new JPanel() {

			@Override
			public void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setColor(Color.black);
				int w = getWidth();
				int h = getHeight();
				g2.fillRect(0, 0, w, h);
				drawOutlineJocke(g2);
				g2.setComposite(AlphaComposite.Clear);
				g2.fillRect(10, 10, w - 20, h - 20);
			}
		};
		c.setPreferredSize(screen.getSize());
		getContentPane().add(c);
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setUndecorated(true);
		setDefaultLookAndFeelDecorated(false);
		//setAlwaysOnTop(true);
		pack();
		setVisible(true);
		com.sun.awt.AWTUtilities.setWindowOpaque(this, false);
	}

	enum DIRECTION {
		LEFT,
		RIGHT,
		UP,
		DOWN
	}

	private int getIndex(int side) {
		int mod = Global.lightLayout.length;
		int val = (Global.lightLayoutClockwise ? -side : side) - Global.lightLayoutOffset;

		return ((val % mod) + mod) % mod;
	}

	private void resetDraw() {
		light = 0;
	}

	private void draw(Graphics2D g, int side) {
		draw(g, side, 0, Global.lightLayout[side]);
	}

	private void draw(Graphics2D g, int side, int start, int stop) {
		DIRECTION direction = DIRECTION.DOWN;
		switch (side) {
			case Side.LEFT:
				direction = Global.lightLayoutClockwise ? DIRECTION.UP : DIRECTION.DOWN;
				break;
			case Side.RIGHT:
				direction = Global.lightLayoutClockwise ? DIRECTION.DOWN : DIRECTION.UP;
				break;
			case Side.TOP:
				direction = Global.lightLayoutClockwise ? DIRECTION.RIGHT : DIRECTION.LEFT;
				break;
			case Side.BOTTOM:
				direction = Global.lightLayoutClockwise ? DIRECTION.LEFT : DIRECTION.RIGHT;
				break;
		}

		draw(g, direction, Global.lightLayout[side], start, stop);
	}

	private void draw(Graphics2D g, DIRECTION direction, int numLights, int start, int stop) {
		int a = 10;
		int h = getHeight() - 2 * a;
		int w = getWidth() - 2 * a;

		float fhd = h / numLights;
		int fwd = w / numLights;
		int hd = Math.round(fhd);
		int wd = Math.round(fwd);

		for (int i = start; i < stop; i++) {
			g.setColor(getColor(light++));
			switch (direction) {
				case UP:
					g.fillRect(w + a, h - Math.round(fhd * i) - a, a, hd);
					break;
				case DOWN:
					g.fillRect(0, Math.round(fhd * i) + a, a, hd);
					break;
				case LEFT:
					g.fillRect(w - Math.round(fwd * i) - a, 0, wd, a);
					break;
				case RIGHT:
					g.fillRect(Math.round(fwd * i) + a, h + a, wd, a);
					break;
			}
		}
	}

	private void drawOutlineJocke(Graphics2D g) {
		resetDraw();

		draw(g, Side.LEFT, 16, Global.lightLayout[Side.LEFT]);
		draw(g, Side.BOTTOM);
		draw(g, Side.RIGHT);
		draw(g, Side.TOP);
		draw(g, Side.LEFT, 0, 16);
	}

	private void drawOutlineTesting(Graphics2D g) {
		resetDraw();

		draw(g, Side.LEFT);
		draw(g, Side.BOTTOM);
		draw(g, Side.RIGHT);
		draw(g, Side.TOP);
	}

	private void drawOutline(Graphics2D g) {
		int a = 10;
		int h = getHeight() - 2 * a;
		int w = getWidth() - 2 * a;
		int light = 0;
		int numSides = Global.lightLayout.length;

		if (numSides > 0) {
			float fhd = h / Global.lightLayout[0];
			int hd = Math.round(fhd);
			for (int i = 0; i < Global.lightLayout[0]; i++) {
				g.setColor(getColor(light));
				g.fillRect(w + a, h - Math.round(fhd * i) - 2 * a, a, hd);
				light++;
			}
		}
		if (numSides > 1) {
			int fwd = w / Global.lightLayout[1];
			int wd = Math.round(fwd);
			for (int i = 0; i < Global.lightLayout[1]; i++) {
				g.setColor(getColor(light));
				g.fillRect(w - Math.round(fwd * i) - a, 0, wd, a);
				light++;
			}
		}
		if (numSides > 2) {
			float fhd = h / Global.lightLayout[2];
			int hd = Math.round(fhd);
			for (int i = 0; i < Global.lightLayout[2]; i++) {
				g.setColor(getColor(light));
				g.fillRect(0, Math.round(fhd * i) + a, a, hd);
				light++;
			}
		}
		if (numSides > 3) {
			int fwd = w / Global.lightLayout[3];
			int wd = Math.round(fwd);
			for (int i = 0; i < Global.lightLayout[3]; i++) {
				g.setColor(getColor(light));
				g.fillRect(Math.round(fwd * i) + a, h + a, wd, a);
				light++;
			}
		}
	}

	private Color getColor(int l) {
		if (colorBuffer[l] == null)
			return Color.black;
		else
			return new Color(colorBuffer[l].red, colorBuffer[l].green, colorBuffer[l].blue);
	}

}