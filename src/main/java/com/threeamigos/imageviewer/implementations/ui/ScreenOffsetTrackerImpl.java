package com.threeamigos.imageviewer.implementations.ui;

import com.threeamigos.imageviewer.interfaces.ui.ScreenOffsetTracker;

public class ScreenOffsetTrackerImpl implements ScreenOffsetTracker {

	private double offsetXPercentage;
	private double offsetYPercentage;

	@Override
	public void setOffsetXPercentage(double offsetXPercentage) {
		this.offsetXPercentage = offsetXPercentage;
	}

	@Override
	public double getOffsetXPercentage() {
		return offsetXPercentage;
	}

	@Override
	public void setOffsetYPercentage(double offsetYPercentage) {
		this.offsetYPercentage = offsetYPercentage;
	}

	@Override
	public double getOffsetYPercentage() {
		return offsetYPercentage;
	}

}
