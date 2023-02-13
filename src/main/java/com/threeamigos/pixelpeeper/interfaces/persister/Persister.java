package com.threeamigos.pixelpeeper.interfaces.persister;

public interface Persister<T> {

	PersistResult load(T entity);

	PersistResult save(T entity);

}
