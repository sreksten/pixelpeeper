package com.threeamigos.imageviewer.implementations.ui;

import com.threeamigos.imageviewer.interfaces.ui.PrioritizedInputConsumer;

public class PrioritizedInputAdapter extends InputAdapter implements PrioritizedInputConsumer {

	private int priority;

	public PrioritizedInputAdapter() {
		this.priority = DEFAULT_PRIORITY;
	}

	public PrioritizedInputAdapter(int priority) {
		this.priority = priority;
	}

	@Override
	public void setPriority(int priority) {
		this.priority = priority;
	}

	@Override
	public int getPriority() {
		return priority;
	}

}
