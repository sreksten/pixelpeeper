package com.threeamigos.imageviewer.interfaces.datamodel;

import java.awt.Graphics2D;
import java.io.File;
import java.util.List;

import com.threeamigos.imageviewer.data.ExifTag;
import com.threeamigos.imageviewer.interfaces.ui.ImageSlice;

public interface DataModel {

	// Preferences part

	public int getPreferredX();

	public void setPreferredX(int x);

	public int getPreferredY();

	public void setPreferredY(int y);

	public int getPreferredWidth();

	public void setPreferredWidth(int width);

	public int getPreferredHeight();

	public void setPreferredHeight(int height);

	public String getLastPath();

	public void setLastPath(String lastPath);

	public boolean isAutorotation();

	public void toggleAutorotation();

	public boolean isTagsVisible();

	public void toggleTagsVisibility();

	public boolean isTagVisible(ExifTag exifTag);

	public void toggleTagVisibility(ExifTag exifTag);

	public void savePreferences();

	// Graphics part

	public void reframe(int width, int height);

	public void repaint(Graphics2D graphics);

	public ImageSlice findImageSlice(int mouseX, int mouseY);

	// Data part

	public void loadFiles(List<File> files);

	public boolean hasLoadedImages();

}
