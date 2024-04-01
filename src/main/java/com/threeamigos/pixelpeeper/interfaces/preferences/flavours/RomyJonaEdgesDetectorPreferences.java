package com.threeamigos.pixelpeeper.interfaces.preferences.flavours;

import com.threeamigos.common.util.interfaces.preferences.Preferences;

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
