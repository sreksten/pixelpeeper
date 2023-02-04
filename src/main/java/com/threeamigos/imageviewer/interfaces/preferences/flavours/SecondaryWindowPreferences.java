package com.threeamigos.imageviewer.interfaces.preferences.flavours;

public interface SecondaryWindowPreferences extends WindowPreferences {

	public static final boolean VISIBLE_DEFAULT = false;

	public void setVisible(boolean visible);

	public boolean isVisible();

}
