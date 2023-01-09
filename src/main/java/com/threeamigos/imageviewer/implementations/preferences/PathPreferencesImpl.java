package com.threeamigos.imageviewer.implementations.preferences;

import java.util.Collections;
import java.util.List;

import com.threeamigos.common.util.interfaces.MessageConsumer;
import com.threeamigos.imageviewer.implementations.ui.AbstractPreferencesImpl;
import com.threeamigos.imageviewer.interfaces.persister.Persister;
import com.threeamigos.imageviewer.interfaces.preferences.PathPreferences;

public class PathPreferencesImpl extends AbstractPreferencesImpl<PathPreferences> implements PathPreferences {

	private String lastPath;
	private List<String> lastFilenames;

	@Override
	protected String getEntityDescription() {
		return "path";
	}

	public PathPreferencesImpl(Persister<PathPreferences> persister, MessageConsumer messageConsumer) {
		super(persister, messageConsumer);

		loadPostConstruct();
	}

	@Override
	public void setLastPath(String path) {
		this.lastPath = path;
	}

	@Override
	public String getLastPath() {
		return lastPath;
	}

	@Override
	protected void loadDefaultValues() {
		lastPath = System.getProperty("user.home");
		lastFilenames = Collections.emptyList();
	}

	@Override
	public void setLastFilenames(List<String> lastFilenames) {
		this.lastFilenames = lastFilenames;
	}

	@Override
	public List<String> getLastFilenames() {
		return lastFilenames;
	}

}
