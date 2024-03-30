package com.threeamigos.pixelpeeper.instances;

import com.threeamigos.pixelpeeper.implementations.ui.FileSelectorImpl;
import com.threeamigos.pixelpeeper.interfaces.ui.FileSelector;

public class FileSelectorInstance {

    private static final FileSelector instance = new FileSelectorImpl(Preferences.SESSION);

    public static FileSelector get() {
        return instance;
    }

    private FileSelectorInstance() {
    }
}
