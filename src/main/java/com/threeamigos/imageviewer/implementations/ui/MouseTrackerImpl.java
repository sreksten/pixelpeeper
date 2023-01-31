package com.threeamigos.imageviewer.implementations.ui;

import java.awt.event.MouseEvent;

import com.threeamigos.imageviewer.interfaces.datamodel.DataModel;
import com.threeamigos.imageviewer.interfaces.ui.MouseTracker;

public class MouseTrackerImpl implements MouseTracker {

	private final DataModel dataModel;

	private boolean dragging;

	private int pointerStartX;
	private int pointerStartY;

	public MouseTrackerImpl(DataModel dataModel) {
		this.dataModel = dataModel;
	}

	@Override
	public void mousePressed(MouseEvent e) {
		dataModel.setActiveSlice(e.getX(), e.getY());
		pointerStartX = e.getX();
		pointerStartY = e.getY();
		dragging = true;
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		dataModel.resetActiveSlice();
		dragging = false;
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		int endX = e.getX();
		int deltaX = pointerStartX - endX;
		int endY = e.getY();
		int deltaY = pointerStartY - endY;

		pointerStartX = endX;
		pointerStartY = endY;

		dataModel.move(deltaX, deltaY);
	}

	@Override
	public boolean isDragging() {
		return dragging;
	}

}
