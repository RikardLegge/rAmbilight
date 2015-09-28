package com.rambilight.core.clientInterface.debug;

import com.rambilight.core.api.Global;
import com.rambilight.core.api.Side;

import javax.swing.*;
import java.awt.*;
import java.util.*;

/*
 * Original source
 * http://stackoverflow.com/questions/1190168/pass-mouse-events-to-applications-behind-from-a-java-ui
 * 
 * Edited to suite my needs. Credit to the original author
 */

enum Direction {
	LEFT,
	RIGHT,
	UP,
	DOWN
}

/**
 * A way of visualizing the light buffer in the same way as the arduino and led strip would.
 * Creates a frame and interface for displaying the light buffer.
 */
public class Visualizer {

	private DisplayMode display = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode();
	private Rectangle   screen  = new Rectangle(0, 0, display.getWidth(), display.getHeight());
	ArduinoEmulator.rgb_color[] colorBuffer;
	int             drawIndex = 0;
	Queue<DrawSide> sides     = new LinkedList<>();

	public Visualizer(ArduinoEmulator.rgb_color[] colorBuffer) {
		this.colorBuffer = colorBuffer;
		drawOutlineJocke();
	}

	public void update() {
		sides.forEach(Visualizer.DrawSide::repaint);
	}

	public void dispose() {
		sides.forEach(Visualizer.DrawSide::removeAll);
		sides.forEach(Visualizer.DrawSide::dispose);
		sides.clear();
	}

	private int addDrawIndex(int number, DrawSide side) {
		sides.add(side);

		int cache = drawIndex;
		drawIndex += number;

		return cache;
	}

	private void drawOutlineJocke() {
		new DrawSide(Side.LEFT, 16, Global.lightLayout[Side.LEFT]);
		new DrawSide(Side.BOTTOM);
		new DrawSide(Side.RIGHT);
		new DrawSide(Side.TOP);
		new DrawSide(Side.LEFT, 0, 16);
	}

	private Color getColor(int l) {
		if (colorBuffer[l] == null)
			return Color.black;
		else
			return new Color(colorBuffer[l].red, colorBuffer[l].green, colorBuffer[l].blue);
	}


	class DrawSide extends JFrame {
		int side;
		int start;
		int stop;
		int offset = 22;

		int lightIndex;

		int depth = 10;

		private void addUI() {
			Component c = new JPanel() {
				@Override
				public void paintComponent(Graphics g) {
					Graphics2D g2 = (Graphics2D) g.create();
					draw(g2);
				}
			};

			c.setPreferredSize(new Dimension((int) (width() * partOfWidth()), (int) (height() * partOfHeight())));

			getContentPane().add(c);

			setTitle("rAmbiligt Visualizer");
			try {
				setIconImage(new ImageIcon(Visualizer.class.getResource("Tray_Active.png")).getImage());
			} catch (Exception ignored) { }

			setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
			setUndecorated(true);
			setDefaultLookAndFeelDecorated(false);
			setAlwaysOnTop(true);
			pack();
			setVisible(true);

			if (side == Side.TOP || side == Side.BOTTOM)
				setLocation(x() + (int) (start * lWidth()), y());
			else
				setLocation(x(), y() + (int) (start * lHeight()));


			System.out.println(getX() + ":" + getY() + " & " + getWidth() + "/" + getHeight());
		}

		public DrawSide(int side) {
			this(side, 0, Global.lightLayout[side]);
		}

		public DrawSide(int side, int start, int stop) {
			this.side = side;
			this.start = start;
			this.stop = stop;

			lightIndex = addDrawIndex(stop - start, this);
			addUI();
		}


		int width() {
			switch (side) {
				case Side.TOP:
				case Side.BOTTOM:
					return (int) screen.getWidth() - 2 * depth;
				case Side.LEFT:
				case Side.RIGHT:
					return depth;
				default:
					return 0;
			}
		}

		int height() {
			switch (side) {
				case Side.TOP:
				case Side.BOTTOM:
					return depth;
				case Side.LEFT:
				case Side.RIGHT:
					return (int) screen.getHeight() - 2 * depth - offset;
				default:
					return 0;
			}
		}

		int x() {
			switch (side) {
				case Side.TOP:
				case Side.BOTTOM:
					return depth;
				case Side.LEFT:
					return 0;
				case Side.RIGHT:
					return (int) screen.getWidth() - depth;
				default:
					return 0;
			}
		}

		int y() {
			switch (side) {
				case Side.TOP:
					return offset;
				case Side.LEFT:
				case Side.RIGHT:
					return depth + offset;
				case Side.BOTTOM:
					return (int) screen.getHeight() - depth;
				default:
					return 0;
			}

		}

		Direction getDirection() {
			switch (side) {
				case Side.LEFT:
					return Global.lightLayoutClockwise ? Direction.UP : Direction.DOWN;
				case Side.RIGHT:
					return Global.lightLayoutClockwise ? Direction.DOWN : Direction.UP;
				case Side.TOP:
					return Global.lightLayoutClockwise ? Direction.RIGHT : Direction.LEFT;
				case Side.BOTTOM:
					return Global.lightLayoutClockwise ? Direction.LEFT : Direction.RIGHT;
				default:
					return Direction.UP;
			}
		}

		float partOfWidth() {
			switch (side) {
				case Side.TOP:
				case Side.BOTTOM:
					return (float) numLights() / Global.lightLayout[side];
				default:
					return 1;
			}
		}

		float partOfHeight() {
			switch (side) {
				case Side.LEFT:
				case Side.RIGHT:
					return (float) numLights() / Global.lightLayout[side];
				default:
					return 1;
			}
		}

		int numLights() {
			return (stop - start);
		}

		float lHeight() {
			return getHeight() / numLights();
		}

		float lWidth() {
			return getWidth() / numLights();
		}

		int widthError() {
			return Math.round(getWidth() * .0015f);
		}

		int heightError() {
			return Math.round(getHeight() * .004f);
		}

		private void draw(Graphics2D g) {
			int light = lightIndex;
			Direction dir = getDirection();

			int w = getWidth();
			int h = getHeight();

			float fwd = lWidth();
			float fhd = lHeight();

			int hd = Math.round(fhd);
			int wd = Math.round(fwd);

			for (int i = 0; i < stop - start; i++) {
				g.setColor(getColor(light++));
				switch (dir) {
					case UP:
						g.fillRect(0, h - Math.round(fhd * (i + 1f)) - heightError(), depth, hd);
						break;
					case DOWN:
						g.fillRect(0, Math.round(fhd * i) + heightError(), depth, hd);
						break;
					case LEFT:
						g.fillRect(w - Math.round(fwd * (i + 1f)) - widthError(), 0, wd, depth);
						break;
					case RIGHT:
						g.fillRect(Math.round(fwd * i) + widthError(), 0, wd, depth);
						break;
				}
			}
		}
	}

}