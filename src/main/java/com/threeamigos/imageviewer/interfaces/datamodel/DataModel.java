package com.threeamigos.imageviewer.interfaces.datamodel;

import java.awt.Graphics2D;
import java.io.File;
import java.util.List;

import com.threeamigos.imageviewer.data.ExifTag;
import com.threeamigos.imageviewer.data.ExifTagVisibility;

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

	public boolean isAutorotation();

	public void toggleAutorotation();

	public boolean isMovementAppliedToAllImages();

	public void toggleMovementAppliedToAllImages();

	public boolean isMovementAppliedToAllImagesTemporarilyInverted();

	public void setMovementAppliedToAllImagesTemporarilyInverted(
			boolean isMovementAppliedToAllImagesTemporarilyInverted);

	public boolean isTagsVisible();

	public void toggleTagsVisibility();

	public boolean isOverridingTagsVisibility();

	public void toggleOverridingTagsVisibility();

	public ExifTagVisibility getTagVisibility(ExifTag exifTag);

	public void setTagVisibility(ExifTag exifTag, ExifTagVisibility visibility);

	public void savePreferences();

	// Graphics part

	public void reframe(int width, int height);

	public void repaint(Graphics2D graphics);

	public void setMovementAppliedToAllImages(boolean movementAppliesToAllFrames);

	public void move(int deltaX, int deltaY);

	public void resetMovement();

	public void setActiveSlice(int x, int y);

	public void resetActiveSlice();

	// Data part

	public void loadFiles(List<File> files);

	public String getLastPath();

	public void setLastPath(String lastPath);

	public boolean hasLoadedImages();

}
