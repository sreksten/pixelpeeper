package com.threeamigos.pixelpeeper.instances;

import com.threeamigos.pixelpeeper.implementations.edgedetect.EdgesDetectorFactoryImpl;
import com.threeamigos.pixelpeeper.interfaces.edgedetect.EdgesDetectorFactory;

public class EdgesDetectorFactoryInstance {

    private static final EdgesDetectorFactory instance = new EdgesDetectorFactoryImpl(Preferences.EDGES_DETECTOR,
            Preferences.CANNY_EDGES_DETECTOR, Preferences.ROMY_JONA_EDGES_DETECTOR);

    public static EdgesDetectorFactory get() {
        return instance;
    }

    private EdgesDetectorFactoryInstance() {
    }
}
