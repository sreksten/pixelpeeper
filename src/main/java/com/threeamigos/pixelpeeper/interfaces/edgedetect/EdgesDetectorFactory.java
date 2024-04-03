package com.threeamigos.pixelpeeper.interfaces.edgedetect;

/**
 * An interface that returns an {@link EdgesDetector} object
 *
 * @author Stefano Reksten
 */
public interface EdgesDetectorFactory {

    EdgesDetector getEdgesDetector();

}
