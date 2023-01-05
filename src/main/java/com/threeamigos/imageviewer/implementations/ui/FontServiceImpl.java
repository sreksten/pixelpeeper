package com.threeamigos.imageviewer.implementations.ui;

import java.awt.Font;
import java.util.HashMap;
import java.util.Map;

import com.threeamigos.imageviewer.interfaces.ui.FontService;

public class FontServiceImpl implements FontService {

	private Map<String, Font> fontMap = new HashMap<>();

	@Override
	public Font getFont(String fontName, int attributes, int fontHeight) {
		String key = new StringBuilder(fontName).append("-").append(attributes).append("-").append(fontHeight)
				.toString();
		return fontMap.computeIfAbsent(key, h -> new Font(fontName, attributes, fontHeight));
	}

}
