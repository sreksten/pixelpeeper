package com.threeamigos.imageviewer.interfaces.preferences.flavours;

import com.threeamigos.imageviewer.interfaces.preferences.Preferences;

public interface BigPointerPreferences extends Preferences {

	public static final boolean BIG_POINTER_VISIBLE_DEFAULT = false;
	public static final int BIG_POINTER_SIZE_DEFAULT = 100;
	public static final float BIG_POINTER_ROTATION_DEFAULT = (float) (5 * Math.PI / 4);

	public void setBigPointerVisible(boolean bigPointerVisible);

	public boolean isBigPointerVisible();

	public void setBigPointerSize(int bigPointerSize);

	public int getBigPointerSize();

	public void setBigPointerRotation(float radians);

	public float getBigPointerRotation();

}
