package com.threeamigos.imageviewer.interfaces.persister;

public interface PersistableHelper<T extends Persistable> {

	public void add(T entity);

	public void persist();

}
