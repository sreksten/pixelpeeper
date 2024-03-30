package com.threeamigos.pixelpeeper.instances;

import com.threeamigos.common.util.implementations.ui.HintsCollectorImpl;
import com.threeamigos.common.util.implementations.ui.HintsSupport;
import com.threeamigos.common.util.implementations.ui.HintsWindowImpl;
import com.threeamigos.common.util.interfaces.ui.HintsCollector;
import com.threeamigos.common.util.interfaces.ui.HintsDisplayer;
import com.threeamigos.pixelpeeper.instances.plugins.GridPluginInstance;
import com.threeamigos.pixelpeeper.interfaces.ui.KeyRegistry;

import static com.threeamigos.pixelpeeper.Main.APPLICATION_NAME;

public class HintsDisplayerInstance {

    private static final HintsDisplayer instance;

    static {
        HintsCollector<String> hintsCollector = new HintsCollectorImpl<>();
        hintsCollector.addHints(KeyRegistry.NO_KEY.getHints());
        hintsCollector.addHints(DataModelInstance.get());
        hintsCollector.addHints(DragAndDropWindowInstance.get());
        hintsCollector.addHints(CursorManagerInstance.get());
        hintsCollector.addHints(GridPluginInstance.get());
        HintsSupport hintsSupport = new HintsSupport(Preferences.HINTS, hintsCollector);
        instance = new HintsWindowImpl(APPLICATION_NAME, hintsSupport);
    }

    public static HintsDisplayer get() {
        return instance;
    }

    private HintsDisplayerInstance() {
    }
}
