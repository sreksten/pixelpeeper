package com.threeamigos.pixelpeeper.interfaces.preferences.flavors;

import com.threeamigos.common.util.interfaces.preferences.Preferences;

/**
 * Preferences for the fake RomyJona edges-detection algorithm. Actually, since
 * Canny has a lot of parameters and Sobel has none, this class is added just to
 * test the host-capabilities of the filter parameters preferences window.
 *
 * @author Stefano Reksten
 */
public interface RomyJonaFilterPreferences extends Preferences {

    int PUPPAMENTO_PREFERENCES_DEFAULT = 3;
    boolean A_NASTRO_PREFERENCES_DEFAULT = true;

    default String getDescription() {
        return "Romy Jona Filter preferences";
    }

    void setPuppamento(int puppamento);

    int getPuppamento();

    void setANastro(boolean aNastro);

    boolean isANastro();

    void loadDefaultValues();

}
