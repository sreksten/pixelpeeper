package com.threeamigos.pixelpeeper.instances;

import com.threeamigos.pixelpeeper.implementations.ui.CursorManagerImpl;
import com.threeamigos.pixelpeeper.interfaces.ui.CursorManager;

public class CursorManagerInstance {

    private static final CursorManager instance = new CursorManagerImpl(Preferences.CURSOR);

    public static CursorManager get() {
        return instance;
    }

    private CursorManagerInstance() {
    }
}
