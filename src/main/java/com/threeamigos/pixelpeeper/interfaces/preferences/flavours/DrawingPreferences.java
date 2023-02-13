package com.threeamigos.pixelpeeper.interfaces.preferences.flavours;

import java.awt.Color;

import com.threeamigos.pixelpeeper.interfaces.preferences.Preferences;

public interface DrawingPreferences extends Preferences {

	public static final int TRANSPARENCY_MIN = 0;
	public static final int TRANSPARENCY_MAX = 100;
	public static final int TRANSPARENCY_DEFAULT = 50;

	public static final Color COLOR_DEFAULT = Color.RED;

	public static final int BRUSH_SIZE_MIN = 5;
	public static final int BRUSH_SIZE_MAX = 50;
	public static final int BRUSH_SIZE_DEFAULT = 20;

	public void setTransparency(int transparency);

	public int getTransparency();

	public void setColor(Color color);

	public Color getColor();

	public void setBrushSize(int brushSize);

	public int getBrushSize();

}
