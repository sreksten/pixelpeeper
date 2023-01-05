package com.threeamigos.imageviewer.interfaces.ui;

import java.awt.Font;

/**
 * Acts as a cache for Font objects.
 *
 * @author Stefano Reksten
 *
 */
public interface FontService {

	public static final String STANDARD_FONT_NAME = "Serif";

	Font getFont(String fontName, int fontAttributes, int fontHeight);

}
