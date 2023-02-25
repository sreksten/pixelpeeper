package com.threeamigos.pixelpeeper.interfaces.datamodel;

import java.io.File;

public interface NamePattern {

	public static final String LEFT_BRACKET = "{";
	
	public static final String RIGHT_BRACKET = "}";
	
	public boolean rename(File file);
	
}
