package com.threeamigos.pixelpeeper.interfaces.preferences.flavours;

import com.threeamigos.common.util.interfaces.preferences.Preferences;

public interface RomyJonaEdgesDetectorPreferences extends Preferences {

	public static final int PUPPAMENTO_PREFERENCES_DEFAULT = 3;
	public static final boolean A_NASTRO_PREFERENCES_DEFAULT = true;

	default String getDescription() {
		return "Romy Jona Edges Detector preferences";
	}

	public void setPuppamento(int puppamento);

	public int getPuppamento();

	public void setANastro(boolean aNastro);

	public boolean isANastro();

	void loadDefaultValues();

}
