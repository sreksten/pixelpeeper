package com.threeamigos.pixelpeeper.implementations.preferences.flavors;

import com.threeamigos.common.util.implementations.BasicPropertyChangeAware;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.DoodlingPreferences;

import java.awt.*;

public class DoodlingPreferencesImpl extends BasicPropertyChangeAware implements DoodlingPreferences {

    private int transparency;
    private Color color;
    private int brushSize;

    @Override
    public void setTransparency(int transparency) {
        this.transparency = transparency;
    }

    @Override
    public int getTransparency() {
        return transparency;
    }

    @Override
    public void setColor(Color color) {
        this.color = color;
    }

    @Override
    public Color getColor() {
        return color;
    }

    @Override
    public void setBrushSize(int brushSize) {
        this.brushSize = brushSize;
    }

    @Override
    public int getBrushSize() {
        return brushSize;
    }

    @Override
    public String getDescription() {
        return "drawing preferences";
    }

    @Override
    public void validate() {
        if (transparency < TRANSPARENCY_MIN || transparency > TRANSPARENCY_MAX) {
            throw new IllegalArgumentException("Invalid transparency level");
        }
        if (brushSize < BRUSH_SIZE_MIN || brushSize > BRUSH_SIZE_MAX) {
            throw new IllegalArgumentException("Invalid brush size");
        }
    }

    @Override
    public void loadDefaultValues() {
        transparency = TRANSPARENCY_DEFAULT;
        brushSize = BRUSH_SIZE_DEFAULT;
        color = DoodlingPreferences.COLOR_DEFAULT;
    }

}
