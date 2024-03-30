package com.threeamigos.pixelpeeper.instances;

import com.threeamigos.pixelpeeper.implementations.ui.DragAndDropWindowImpl;
import com.threeamigos.pixelpeeper.interfaces.ui.DragAndDropWindow;

public class DragAndDropWindowInstance {

    private static final DragAndDropWindow instance = new DragAndDropWindowImpl(Preferences.DRAG_AND_DROP_WINDOW, ExifCacheInstance.get(),
            FontServiceInstance.get(), MessageHandlerInstance.get());

    public static DragAndDropWindow get() {
        return instance;
    }

    private DragAndDropWindowInstance() {
    }
}
