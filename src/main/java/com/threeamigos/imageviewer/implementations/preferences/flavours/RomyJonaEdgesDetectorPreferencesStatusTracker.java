package com.threeamigos.imageviewer.implementations.preferences.flavours;

import com.threeamigos.imageviewer.interfaces.StatusTracker;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.RomyJonaEdgesDetectorPreferences;

public class RomyJonaEdgesDetectorPreferencesStatusTracker implements StatusTracker<RomyJonaEdgesDetectorPreferences> {

	private int puppamentoAtStart;
	private boolean aNastroAtStart;

	private final RomyJonaEdgesDetectorPreferences romyJonaEdgesDetectorPreferences;

	public RomyJonaEdgesDetectorPreferencesStatusTracker(RomyJonaEdgesDetectorPreferences romyJonaEdgesDetectorPreferences) {
		this.romyJonaEdgesDetectorPreferences = romyJonaEdgesDetectorPreferences;
	}

	@Override
	public void loadInitialValues() {
		puppamentoAtStart = romyJonaEdgesDetectorPreferences.getPuppamento();
		aNastroAtStart = romyJonaEdgesDetectorPreferences.isANastro();
	}

	@Override
	public boolean hasChanged() {
		return romyJonaEdgesDetectorPreferences.getPuppamento() != puppamentoAtStart
				|| romyJonaEdgesDetectorPreferences.isANastro() != aNastroAtStart;
	}

}