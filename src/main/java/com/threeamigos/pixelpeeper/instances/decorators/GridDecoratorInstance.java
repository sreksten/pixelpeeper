package com.threeamigos.pixelpeeper.instances.decorators;

import com.threeamigos.pixelpeeper.implementations.ui.imagedecorators.GridDecorator;
import com.threeamigos.pixelpeeper.instances.Preferences;

public class GridDecoratorInstance {

    private static final GridDecorator instance = new GridDecorator(Preferences.MAIN_WINDOW, Preferences.GRID);

    public static GridDecorator get() {
        return instance;
    }

    private GridDecoratorInstance() {
    }
}
