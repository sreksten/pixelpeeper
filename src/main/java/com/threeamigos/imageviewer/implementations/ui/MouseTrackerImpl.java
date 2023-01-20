package com.threeamigos.imageviewer.implementations.ui;

import java.awt.event.MouseEvent;

import com.threeamigos.imageviewer.interfaces.datamodel.DataModel;
import com.threeamigos.imageviewer.interfaces.ui.MouseTracker;

public class MouseTrackerImpl implements MouseTracker {

	private final DataModel dataModel;

	private int pointerX;
	private int pointerY;

	private int pointerStartX;
	private int pointerStartY;

	public MouseTrackerImpl(DataModel dataModel) {
		this.dataModel = dataModel;
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		pointerX = e.getX();
		pointerY = e.getY();
	}

	@Override
	public void mousePressed(MouseEvent e) {
		dataModel.setActiveSlice(e.getX(), e.getY());
		pointerStartX = e.getX();
		pointerStartY = e.getY();
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		dataModel.resetActiveSlice();
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		int endX = e.getX();
		int deltaX = pointerStartX - endX;
		int endY = e.getY();
		int deltaY = pointerStartY - endY;

		dataModel.move(deltaX, deltaY);

		pointerX = endX;
		pointerY = endY;

		pointerStartX = endX;
		pointerStartY = endY;
	}

	@Override
	public int getPointerX() {
		return pointerX;
	}

	@Override
	public int getPointerY() {
		return pointerY;
	}

}
