package com.threeamigos.imageviewer.interfaces.ui;

/**
 * Keeps track of the images' shift
 *
 * @author Stefano Reksten
 *
 */
public interface ScreenOffsetTracker {

	public void setOffsetXPercentage(double offsetXPercentage);

	public double getOffsetXPercentage();

	public void setOffsetYPercentage(double offsetYPercentage);

	public double getOffsetYPercentage();

	public void reset();

}
