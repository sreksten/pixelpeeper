package com.threeamigos.imageviewer.interfaces.ui;

import java.awt.event.MouseEvent;

/**
 * Listens to mouse movements to update the images' shift
 *
 * @author Stefano Reksten
 *
 */
public interface MouseTracker {

	public void mousePressed(MouseEvent e);

	public void mouseReleased(MouseEvent e);

	public void mouseDragged(MouseEvent e);

	public boolean isDragging();

}
