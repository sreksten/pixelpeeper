package com.threeamigos.imageviewer.implementations.preferences;

import com.threeamigos.common.util.interfaces.ErrorMessageHandler;
import com.threeamigos.imageviewer.implementations.ui.AbstractPreferencesImpl;
import com.threeamigos.imageviewer.interfaces.persister.Persister;
import com.threeamigos.imageviewer.interfaces.preferences.RomyJonaEdgesDetectorPreferences;

public class RomyJonaEdgesDetectorPreferencesImpl extends AbstractPreferencesImpl<RomyJonaEdgesDetectorPreferences>
		implements RomyJonaEdgesDetectorPreferences {

	private int puppamentoAtStart;
	private boolean aNastroAtStart;

	private int puppamento;
	private boolean aNastro;

	@Override
	protected String getEntityDescription() {
		return "romy jona edge detector";
	}

	public RomyJonaEdgesDetectorPreferencesImpl(Persister<RomyJonaEdgesDetectorPreferences> persister,
			ErrorMessageHandler errorMessageHandler) {
		super(persister, errorMessageHandler);

		loadPostConstruct();
		copyPreferencesAtStart();
	}

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
	protected void loadDefaultValues() {
		puppamento = RomyJonaEdgesDetectorPreferences.PUPPAMENTO_PREFERENCES_DEFAULT;
		aNastro = RomyJonaEdgesDetectorPreferences.A_NASTRO_PREFERENCES_DEFAULT;
	}

	private void copyPreferencesAtStart() {
		puppamentoAtStart = puppamento;
		aNastroAtStart = aNastro;
	}

	@Override
	public boolean hasChanged() {
		return puppamento != puppamentoAtStart || aNastro != aNastroAtStart;
	}
}
