package com.threeamigos.imageviewer.interfaces.preferences;

import com.threeamigos.imageviewer.interfaces.persister.Persistable;

public interface RomyJonaEdgesDetectorPreferences extends Persistable {

	public static final int PUPPAMENTO_PREFERENCES_DEFAULT = 5;
	public static final boolean A_NASTRO_PREFERENCES_DEFAULT = true;

	public void setPuppamento(int puppamento);

	public int getPuppamento();

	public void setANastro(boolean aNastro);

	public boolean isANastro();

}
