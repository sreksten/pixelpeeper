package com.threeamigos.pixelpeeper.interfaces.preferences.flavours;

import com.threeamigos.common.util.interfaces.preferences.Preferences;

/**
 * Preferences for the fake RomyJona edges-detection algorithm. Actually, since
 * Canny has a lot of parameters and Sobel has none, this class is added just to
 * test the host-capabilities of the edge-detector parameters preference window.
 *
 * @author Stefano Reksten
 */
public interface RomyJonaEdgesDetectorPreferences extends Preferences {

    int PUPPAMENTO_PREFERENCES_DEFAULT = 3;
    boolean A_NASTRO_PREFERENCES_DEFAULT = true;

    default String getDescription() {
        return "Romy Jona Edges Detector preferences";
    }

    void setPuppamento(int puppamento);

    int getPuppamento();

    void setANastro(boolean aNastro);

    boolean isANastro();

    void loadDefaultValues();

}
