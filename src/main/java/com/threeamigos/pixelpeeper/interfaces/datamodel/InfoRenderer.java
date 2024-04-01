package com.threeamigos.pixelpeeper.interfaces.datamodel;

import java.awt.*;

public interface InfoRenderer {

    int HSPACING = 5;
    int VSPACING = 5;

    int FILENAME_FONT_HEIGHT = 32;
    int TAG_FONT_HEIGHT = 16;

    void reset();

    void render(Graphics g, int x, int y);

}
