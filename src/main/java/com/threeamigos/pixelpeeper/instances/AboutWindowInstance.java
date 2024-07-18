package com.threeamigos.pixelpeeper.instances;

import com.threeamigos.common.util.implementations.ui.AboutWindowImpl;
import com.threeamigos.common.util.interfaces.ui.AboutWindow;

import static com.threeamigos.pixelpeeper.Main.APPLICATION_NAME;

public class AboutWindowInstance {

    private static final AboutWindow instance = new AboutWindowImpl(APPLICATION_NAME,
            "by Stefano Reksten - stefano.reksten@gmail.com",
            "Released under the Apache License v2.0",
            "Uses Drew Noakes' Metadata Extractor",
            "Uses Tom Gibara's Canny Edge Detect implementation");

    public static AboutWindow get() {
        return instance;
    }

    private AboutWindowInstance() {
    }
}
