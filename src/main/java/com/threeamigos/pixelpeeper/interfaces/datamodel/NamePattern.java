package com.threeamigos.pixelpeeper.interfaces.datamodel;

import java.io.File;

public interface NamePattern {

    String LEFT_BRACKET = "{";

    String RIGHT_BRACKET = "}";

    boolean rename(File file);

}
