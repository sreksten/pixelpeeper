package com.threeamigos.imageviewer.implementations.ui.imagedecorators;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.geom.AffineTransform;

import com.threeamigos.imageviewer.interfaces.preferences.flavours.BigPointerPreferences;
import com.threeamigos.imageviewer.interfaces.ui.ImageDecorator;
import com.threeamigos.imageviewer.interfaces.ui.MouseTracker;

public class BigPointerDecorator implements ImageDecorator {

	private final BigPointerPreferences pointerPreferences;
	private final MouseTracker mouseTracker;

	public BigPointerDecorator(BigPointerPreferences pointerPreferences, MouseTracker mouseTracker) {
		this.pointerPreferences = pointerPreferences;
		this.mouseTracker = mouseTracker;
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

		int x = mouseTracker.getPointerX();
		int y = mouseTracker.getPointerY();

		Polygon arrow = new Polygon();
		for (int i = 0; i < vertexes.length; i++) {
			Point vertex = vertexes[i];
			arrow.addPoint(x + (int) vertex.getX(), y + (int) vertex.getY());
		}

		AffineTransform transform = new AffineTransform();
		transform.rotate(pointerPreferences.getBigPointerRotation(), x, y);
		graphics.fill(transform.createTransformedShape(arrow));
	}

}
