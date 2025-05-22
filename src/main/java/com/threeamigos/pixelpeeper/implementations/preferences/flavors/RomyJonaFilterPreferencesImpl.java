package com.threeamigos.pixelpeeper.implementations.preferences.flavors;

import com.threeamigos.common.util.implementations.BasicPropertyChangeAware;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.RomyJonaFilterPreferences;

public class RomyJonaFilterPreferencesImpl extends BasicPropertyChangeAware
        implements RomyJonaFilterPreferences {

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
        puppamento = RomyJonaFilterPreferences.PUPPAMENTO_PREFERENCES_DEFAULT;
        aNastro = RomyJonaFilterPreferences.A_NASTRO_PREFERENCES_DEFAULT;
    }
}
