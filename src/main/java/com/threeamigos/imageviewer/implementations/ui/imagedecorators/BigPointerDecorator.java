package com.threeamigos.imageviewer.implementations.ui.imagedecorators;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import com.threeamigos.imageviewer.implementations.ui.InputAdapter;
import com.threeamigos.imageviewer.interfaces.datamodel.CommunicationMessages;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.BigPointerPreferences;
import com.threeamigos.imageviewer.interfaces.ui.ImageDecorator;
import com.threeamigos.imageviewer.interfaces.ui.InputConsumer;
import com.threeamigos.imageviewer.interfaces.ui.MouseTracker;

public class BigPointerDecorator implements ImageDecorator {

	private final BigPointerPreferences pointerPreferences;
	private final MouseTracker mouseTracker;

	private int x;
	private int y;

	private boolean pointerInFrame;

	private final PropertyChangeSupport propertyChangeSupport;

	public BigPointerDecorator(BigPointerPreferences pointerPreferences, MouseTracker mouseTracker) {
		this.pointerPreferences = pointerPreferences;
		this.mouseTracker = mouseTracker;

		propertyChangeSupport = new PropertyChangeSupport(this);
	}

	@Override
	public void paint(Graphics2D graphics) {

		if (!pointerInFrame) {
			return;
		}

		if (pointerPreferences.isBigPointerVisible() && !mouseTracker.isDragging()) {

			Color previousColor = graphics.getColor();

			int size = pointerPreferences.getBigPointerSize();

			Point[] vertexes = new Point[7];

			vertexes[0] = new Point(0, 0);
			vertexes[1] = new Point(size * -79 / 100, size * 30 / 100);
			vertexes[2] = new Point(size * -69 / 100, size * 10 / 100);
			vertexes[3] = new Point(-size, size * 10 / 100);
			vertexes[4] = new Point(-size, size * -10 / 100);
			vertexes[5] = new Point(size * -69 / 100, size * -10 / 100);
			vertexes[6] = new Point(size * -79 / 100, size * -30 / 100);

			graphics.setColor(Color.BLACK);

			drawPolygon(graphics, vertexes);

			vertexes[0] = new Point(size * -5 / 100, 0);
			vertexes[1] = new Point(size * -76 / 100, size * 27 / 100);
			vertexes[2] = new Point(size * -66 / 100, size * 8 / 100);
			vertexes[3] = new Point(size * -98 / 100, size * 8 / 100);
			vertexes[4] = new Point(size * -98 / 100, size * -8 / 100);
			vertexes[5] = new Point(size * -66 / 100, size * -8 / 100);
			vertexes[6] = new Point(size * -76 / 100, size * -27 / 100);

			graphics.setColor(Color.WHITE);

			drawPolygon(graphics, vertexes);

			graphics.setColor(previousColor);

		}

		if (pointerPreferences.isBigPointerVisible() && mouseTracker.isDragging()) {

			Color previousColor = graphics.getColor();

			int size = pointerPreferences.getBigPointerSize();

			Point[] vertexes = new Point[19];

			vertexes[0] = new Point(size * 18 / 100, size * 11 / 100);
			vertexes[1] = new Point(size * 25 / 100, size * 5 / 100);
			vertexes[2] = new Point(size * 33 / 100, size * 5 / 100);
			vertexes[3] = new Point(size * 37 / 100, size * 9 / 100);
			vertexes[4] = new Point(size * 40 / 100, size * 4 / 100);
			vertexes[5] = new Point(size * 47 / 100, size * 4 / 100);
			vertexes[6] = new Point(size * 51 / 100, size * 7 / 100);
			vertexes[7] = new Point(size * 54 / 100, size * 5 / 100);
			vertexes[8] = new Point(size * 62 / 100, size * 5 / 100);
			vertexes[9] = new Point(size * 66 / 100, size * 11 / 100);
			vertexes[10] = new Point(size * 69 / 100, size * 10 / 100);
			vertexes[11] = new Point(size * 75 / 100, size * 10 / 100);
			vertexes[12] = new Point(size * 85 / 100, size * 34 / 100);
			vertexes[13] = new Point(size * 85 / 100, size * 78 / 100);
			vertexes[14] = new Point(size * 70 / 100, size * 94 / 100);
			vertexes[15] = new Point(size * 18 / 100, size * 94 / 100);
			vertexes[16] = new Point(size * 6 / 100, size * 72 / 100);
			vertexes[17] = new Point(size * 8 / 100, size * 39 / 100);
			vertexes[18] = new Point(size * 18 / 100, size * 33 / 100);

			graphics.setColor(Color.BLACK);

			Polygon arrow = new Polygon();
			for (int i = 0; i < vertexes.length; i++) {
				Point vertex = vertexes[i];
				arrow.addPoint(x + (int) vertex.getX(), y + (int) vertex.getY());
			}
			graphics.fill(arrow);

			vertexes[0] = new Point(size * 24 / 100, size * 14 / 100);
			vertexes[1] = new Point(size * 28 / 100, size * 10 / 100);
			vertexes[2] = new Point(size * 32 / 100, size * 10 / 100);
			vertexes[3] = new Point(size * 37 / 100, size * 14 / 100);
			vertexes[4] = new Point(size * 42 / 100, size * 10 / 100);
			vertexes[5] = new Point(size * 48 / 100, size * 10 / 100);
			vertexes[6] = new Point(size * 52 / 100, size * 15 / 100);
			vertexes[7] = new Point(size * 56 / 100, size * 10 / 100);
			vertexes[8] = new Point(size * 60 / 100, size * 10 / 100);
			vertexes[9] = new Point(size * 66 / 100, size * 18 / 100);
			vertexes[10] = new Point(size * 69 / 100, size * 16 / 100);
			vertexes[11] = new Point(size * 73 / 100, size * 16 / 100);
			vertexes[12] = new Point(size * 80 / 100, size * 36 / 100);
			vertexes[13] = new Point(size * 80 / 100, size * 75 / 100);
			vertexes[14] = new Point(size * 66 / 100, size * 89 / 100);
			vertexes[15] = new Point(size * 23 / 100, size * 89 / 100);
			vertexes[16] = new Point(size * 12 / 100, size * 69 / 100);
			vertexes[17] = new Point(size * 14 / 100, size * 42 / 100);
			vertexes[18] = new Point(size * 24 / 100, size * 36 / 100);

			graphics.setColor(Color.WHITE);

			arrow = new Polygon();
			for (int i = 0; i < vertexes.length; i++) {
				Point vertex = vertexes[i];
				arrow.addPoint(x + (int) vertex.getX(), y + (int) vertex.getY());
			}
			graphics.fill(arrow);

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

	public InputConsumer getInputConsumer() {
		return new InputAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {

				if (e.getKeyCode() == KeyEvent.VK_NUMPAD5) {
					pointerPreferences.setBigPointerVisible(!pointerPreferences.isBigPointerVisible());
					propertyChangeSupport.firePropertyChange(CommunicationMessages.BIG_POINTER_CHANGE, null, this);

				} else if (pointerPreferences.isBigPointerVisible()) {
					Float rotation = null;
					if (e.getKeyCode() == KeyEvent.VK_NUMPAD1) {
						rotation = (float) (7 * Math.PI / 4);
					} else if (e.getKeyCode() == KeyEvent.VK_NUMPAD2) {
						rotation = (float) (6 * Math.PI / 4);
					} else if (e.getKeyCode() == KeyEvent.VK_NUMPAD3) {
						rotation = (float) (5 * Math.PI / 4);
					} else if (e.getKeyCode() == KeyEvent.VK_NUMPAD4) {
						rotation = 0f;
					} else if (e.getKeyCode() == KeyEvent.VK_NUMPAD6) {
						rotation = (float) (Math.PI);
					} else if (e.getKeyCode() == KeyEvent.VK_NUMPAD7) {
						rotation = (float) (Math.PI / 4);
					} else if (e.getKeyCode() == KeyEvent.VK_NUMPAD8) {
						rotation = (float) (Math.PI / 2);
					} else if (e.getKeyCode() == KeyEvent.VK_NUMPAD9) {
						rotation = (float) (3 * Math.PI / 4);
					}
					if (rotation != null) {
						pointerPreferences.setBigPointerRotation(rotation);
						propertyChangeSupport.firePropertyChange(CommunicationMessages.REQUEST_REPAINT, null, this);
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

			@Override
			public void mouseEntered(MouseEvent e) {
				pointerInFrame = true;
			}

			@Override
			public void mouseExited(MouseEvent e) {
				pointerInFrame = false;
				propertyChangeSupport.firePropertyChange(CommunicationMessages.REQUEST_REPAINT, null, this);
			}
		};
	}

	public void addPropertyChangeListener(PropertyChangeListener pcl) {
		propertyChangeSupport.addPropertyChangeListener(pcl);
	}

	public void removePropertyChangeListener(PropertyChangeListener pcl) {
		propertyChangeSupport.removePropertyChangeListener(pcl);
	}

}
