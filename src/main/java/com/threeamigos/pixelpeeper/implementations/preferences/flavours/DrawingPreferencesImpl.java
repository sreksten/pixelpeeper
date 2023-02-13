package com.threeamigos.pixelpeeper.implementations.preferences.flavours;

import java.awt.Color;

import com.threeamigos.pixelpeeper.interfaces.preferences.flavours.DrawingPreferences;

public class DrawingPreferencesImpl extends PropertyChangeAwareImpl implements DrawingPreferences {

	private int transparency;
	private Color color;
	private int brushSize;

	@Override
	public void setTransparency(int transparency) {
		this.transparency = transparency;
	}

	@Override
	public int getTransparency() {
		return transparency;
	}

	@Override
	public void setColor(Color color) {
		this.color = color;
	}

	@Override
	public Color getColor() {
		return color;
	}

	@Override
	public void setBrushSize(int brushSize) {
		this.brushSize = brushSize;
	}

	@Override
	public int getBrushSize() {
		return brushSize;
	}

	@Override
	public String getDescription() {
		return "drawing preferences";
	}

	@Override
	public void validate() {
		if (transparency < TRANSPARENCY_MIN || transparency > TRANSPARENCY_MAX) {
			throw new IllegalArgumentException("Invalid transparency level");
		}
		if (brushSize < BRUSH_SIZE_MIN || brushSize > BRUSH_SIZE_MAX) {
			throw new IllegalArgumentException("Invalid brush size");
		}
	}

	@Override
	public void loadDefaultValues() {
		transparency = TRANSPARENCY_DEFAULT;
		brushSize = BRUSH_SIZE_DEFAULT;
		color = null;
	}

}
