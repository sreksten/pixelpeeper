package com.threeamigos.pixelpeeper.instances;

import com.threeamigos.pixelpeeper.implementations.ui.ShortcutsWindowImpl;
import com.threeamigos.pixelpeeper.interfaces.ui.ShortcutsWindow;

public class ShortcutsWindowInstance {

    private static final ShortcutsWindow instance = new ShortcutsWindowImpl(Preferences.SHORTCUTS_WINDOW);

    public static ShortcutsWindow get() {
        return instance;
    }

    private ShortcutsWindowInstance() {
    }
}
