package com.threeamigos.pixelpeeper.instances;

import com.threeamigos.pixelpeeper.implementations.edgedetect.ui.EdgesDetectorPreferencesSelectorFactoryImpl;
import com.threeamigos.pixelpeeper.interfaces.edgedetect.ui.EdgesDetectorPreferencesSelectorFactory;

public class EdgesDetectorPreferencesSelectorFactoryInstance {

    private static final EdgesDetectorPreferencesSelectorFactory instance =
            new EdgesDetectorPreferencesSelectorFactoryImpl(Preferences.EDGES_DETECTOR,
                    Preferences.CANNY_EDGES_DETECTOR, Preferences.ROMY_JONA_EDGES_DETECTOR,
                    Preferences.ZX_SPECTRUM_EDGES_DETECTOR, Preferences.C64_EDGES_DETECTOR,
                    DataModelInstance.get(), ExifImageReaderInstance.get(), MessageHandlerInstance.get());

    public static EdgesDetectorPreferencesSelectorFactory get() {
        return instance;
    }

    private EdgesDetectorPreferencesSelectorFactoryInstance() {
    }
}
