package com.threeamigos.imageviewer.interfaces.persister;

public interface Persister<T> {

	public PersistResult load(T entity);

	public PersistResult save(T entity);

}
