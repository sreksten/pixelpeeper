package com.threeamigos.pixelpeeper.interfaces.preferences.flavours;

import com.threeamigos.common.util.interfaces.preferences.Preferences;

import java.awt.*;

public interface DrawingPreferences extends Preferences {

    int TRANSPARENCY_MIN = 0;
    int TRANSPARENCY_MAX = 100;
    int TRANSPARENCY_DEFAULT = 50;

    Color COLOR_DEFAULT = Color.RED;

    int BRUSH_SIZE_MIN = 5;
    int BRUSH_SIZE_MAX = 50;
    int BRUSH_SIZE_DEFAULT = 20;

    void setTransparency(int transparency);

    int getTransparency();

    void setColor(Color color);

    Color getColor();

    void setBrushSize(int brushSize);

    int getBrushSize();

}
