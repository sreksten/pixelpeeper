package com.threeamigos.imageviewer.implementations.ui.imagedecorators;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;

import com.threeamigos.imageviewer.implementations.ui.PrioritizedInputAdapter;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.BigPointerPreferences;
import com.threeamigos.imageviewer.interfaces.ui.ImageDecorator;
import com.threeamigos.imageviewer.interfaces.ui.PrioritizedInputConsumer;

public class BigPointerDecorator implements ImageDecorator {

	private final BigPointerPreferences pointerPreferences;
	private int x;
	private int y;

	public BigPointerDecorator(BigPointerPreferences pointerPreferences) {
		this.pointerPreferences = pointerPreferences;
	}

	@Override
	public void paint(Graphics2D graphics) {

		if (pointerPreferences.isBigPointerVisible()) {

			Color previousColor = graphics.getColor();

			int size = pointerPreferences.getBigPointerSize();

			Point[] vertexes = new Point[7];

			vertexes[0] = new Point(0, 0);
			vertexes[1] = new Point(size * -80 / 100, size * 30 / 100);
			vertexes[2] = new Point(size * -70 / 100, size * 10 / 100);
			vertexes[3] = new Point(-size, size * 10 / 100);
			vertexes[4] = new Point(-size, size * -10 / 100);
			vertexes[5] = new Point(size * -70 / 100, size * -10 / 100);
			vertexes[6] = new Point(size * -80 / 100, size * -30 / 100);

			graphics.setColor(Color.BLACK);

			drawPolygon(graphics, vertexes);

			vertexes[0] = new Point(size * -8 / 100, 0);
			vertexes[1] = new Point(size * -75 / 100, size * 25 / 100);
			vertexes[2] = new Point(size * -65 / 100, size * 8 / 100);
			vertexes[3] = new Point(size * -98 / 100, size * 8 / 100);
			vertexes[4] = new Point(size * -98 / 100, size * -8 / 100);
			vertexes[5] = new Point(size * -65 / 100, size * -8 / 100);
			vertexes[6] = new Point(size * -75 / 100, size * -25 / 100);

			graphics.setColor(Color.WHITE);

			drawPolygon(graphics, vertexes);

			graphics.setColor(previousColor);

		}
	}

	private void drawPolygon(Graphics2D graphics, Point[] vertexes) {

		Polygon arrow = new Polygon();
		for (int i = 0; i < vertexes.length; i++) {
			Point vertex = vertexes[i];
			arrow.addPoint(x + (int) vertex.getX(), y + (int) vertex.getY());
		}

		AffineTransform transform = new AffineTransform();
		transform.rotate(pointerPreferences.getBigPointerRotation(), x, y);
		graphics.fill(transform.createTransformedShape(arrow));
	}

	public PrioritizedInputConsumer getPrioritizedInputConsumer() {

		return new PrioritizedInputAdapter(5) {

			@Override
			public void keyPressed(KeyEvent e) {
				if (pointerPreferences.isBigPointerVisible()) {
					if (e.getKeyCode() == KeyEvent.VK_NUMPAD1) {
						pointerPreferences.setBigPointerRotation((float) (3 * Math.PI / 4));
					} else if (e.getKeyCode() == KeyEvent.VK_NUMPAD2) {
						pointerPreferences.setBigPointerRotation((float) (Math.PI / 2));
					} else if (e.getKeyCode() == KeyEvent.VK_NUMPAD3) {
						pointerPreferences.setBigPointerRotation((float) (Math.PI / 4));
					} else if (e.getKeyCode() == KeyEvent.VK_NUMPAD4) {
						pointerPreferences.setBigPointerRotation((float) (Math.PI));
					} else if (e.getKeyCode() == KeyEvent.VK_NUMPAD6) {
						pointerPreferences.setBigPointerRotation(0);
					} else if (e.getKeyCode() == KeyEvent.VK_NUMPAD7) {
						pointerPreferences.setBigPointerRotation((float) (5 * Math.PI / 4));
					} else if (e.getKeyCode() == KeyEvent.VK_NUMPAD8) {
						pointerPreferences.setBigPointerRotation((float) (6 * Math.PI / 4));
					} else if (e.getKeyCode() == KeyEvent.VK_NUMPAD9) {
						pointerPreferences.setBigPointerRotation((float) (7 * Math.PI / 4));
					}
				}
			}

			@Override
			public void mouseMoved(MouseEvent e) {
				x = e.getX();
				y = e.getY();
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				x = e.getX();
				y = e.getY();
			}

		};

	}

}
