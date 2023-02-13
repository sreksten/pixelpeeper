package com.threeamigos.pixelpeeper.implementations.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.threeamigos.pixelpeeper.interfaces.ui.HintsCollector;
import com.threeamigos.pixelpeeper.interfaces.ui.HintsProducer;

public class HintsCollectorImpl implements HintsCollector {

	private List<String> hints = new ArrayList<>();

	@Override
	public void addHint(String hint) {
		this.hints.add(hint);
	}

	@Override
	public void addHints(Collection<String> hints) {
		this.hints.addAll(hints);
	}

	@Override
	public void addHints(HintsProducer hintsProducer) {
		this.hints.addAll(hintsProducer.getHints());
	}

	@Override
	public Collection<String> getHints() {
		return Collections.unmodifiableCollection(hints);
	}

}
