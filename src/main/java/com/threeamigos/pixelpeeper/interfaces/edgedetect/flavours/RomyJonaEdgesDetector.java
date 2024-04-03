package com.threeamigos.pixelpeeper.interfaces.edgedetect.flavours;

import com.threeamigos.pixelpeeper.interfaces.edgedetect.EdgesDetector;

/**
 * An interface exposing all parameters the fake RomyJona edge-detection algorithm accepts.
 * Used to test the hosting capabilities of the edges detector preferences window.
 *
 * @author Stefano Reksten
 */
public interface RomyJonaEdgesDetector extends EdgesDetector {

    void setPuppamento(int puppamento);

    int getPuppamento();

    void setANastro(boolean aNastro);

    boolean isANastro();

}
