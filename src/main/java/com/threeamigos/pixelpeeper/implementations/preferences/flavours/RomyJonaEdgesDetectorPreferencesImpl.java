package com.threeamigos.pixelpeeper.implementations.preferences.flavours;

import com.threeamigos.common.util.implementations.BasicPropertyChangeAware;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavours.RomyJonaEdgesDetectorPreferences;

public class RomyJonaEdgesDetectorPreferencesImpl extends BasicPropertyChangeAware
        implements RomyJonaEdgesDetectorPreferences {

    private int puppamento;
    private boolean aNastro;

    @Override
    public void setPuppamento(int puppamento) {
        this.puppamento = puppamento;
    }

    @Override
    public int getPuppamento() {
        return puppamento;
    }

    @Override
    public void setANastro(boolean aNastro) {
        this.aNastro = aNastro;
    }

    @Override
    public boolean isANastro() {
        return aNastro;
    }

    @Override
    public void loadDefaultValues() {
        puppamento = RomyJonaEdgesDetectorPreferences.PUPPAMENTO_PREFERENCES_DEFAULT;
        aNastro = RomyJonaEdgesDetectorPreferences.A_NASTRO_PREFERENCES_DEFAULT;
    }
}
