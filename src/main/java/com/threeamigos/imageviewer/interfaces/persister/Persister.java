package com.threeamigos.imageviewer.interfaces.persister;

public interface Persister<T> {

	PersistResult load(T entity);

	PersistResult save(T entity);

}
