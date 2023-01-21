package com.threeamigos.imageviewer.interfaces.ui;

public interface PrioritizedInputConsumer extends InputConsumer {

	public static final int DEFAULT_PRIORITY = 0;

	public void setPriority(int priority);

	public int getPriority();

}
