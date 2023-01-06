package com.threeamigos.imageviewer.implementations.preferences;

import com.threeamigos.imageviewer.implementations.ui.AbstractPreferencesImpl;
import com.threeamigos.imageviewer.interfaces.persister.Persister;
import com.threeamigos.imageviewer.interfaces.preferences.PathPreferences;

public class PathPreferencesImpl extends AbstractPreferencesImpl<PathPreferences> implements PathPreferences {

	private String path;

	@Override
	protected String getEntityDescription() {
		return "path";
	}

	public PathPreferencesImpl(Persister<PathPreferences> persister) {
		super(persister);

		loadPostConstruct();
	}

	@Override
	public void setLastPath(String path) {
		this.path = path;
	}

	@Override
	public String getLastPath() {
		return path;
	}

	@Override
	protected void loadDefaultValues() {
		path = System.getProperty("user.home");
	}

}
