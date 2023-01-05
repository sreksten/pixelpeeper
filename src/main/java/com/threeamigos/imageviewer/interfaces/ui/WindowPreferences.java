package com.threeamigos.imageviewer.interfaces.ui;

import com.threeamigos.imageviewer.interfaces.persister.Persistable;

/**
 * Keeps track of the window dimension and position
 *
 * @author Stefano Reksten
 *
 */
public interface WindowPreferences extends Persistable {

	public static final boolean AUTOROTATION_DEFAULT = true;
	public static final boolean TAGS_VISIBLE_DEFAULT = true;

	public void setWidth(int width);

	public int getWidth();

	public void setHeight(int height);

	public int getHeight();

	public void setX(int x);

	public int getX();

	public void setY(int y);

	public int getY();

	public void setAutorotation(boolean autorotation);

	public boolean isAutorotation();

	public void setTagsVisible(boolean tagsVisible);

	public boolean isTagsVisible();

}
