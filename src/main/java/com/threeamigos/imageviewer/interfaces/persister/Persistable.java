package com.threeamigos.imageviewer.interfaces.persister;

public interface Persistable {

	public boolean hasChanged();

	public void persist();

}
