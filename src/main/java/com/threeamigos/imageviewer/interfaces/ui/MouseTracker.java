package com.threeamigos.imageviewer.interfaces.ui;

import java.awt.event.MouseEvent;

/**
 * Listens to mouse movements to update the images' shift
 *
 * @author Stefano Reksten
 *
 */
public interface MouseTracker {

	public void mousePressed(MouseEvent e, ImageSlice slice);

	public void mouseReleased(MouseEvent e);

	public void mouseDragged(MouseEvent e);

}
