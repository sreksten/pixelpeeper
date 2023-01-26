package com.threeamigos.imageviewer.interfaces.ui;

import java.util.Collection;

public interface HintsCollector {

	public void addHint(String hint);

	public void addHints(Collection<String> hints);

	public void addHints(HintsProducer hintsProducer);

	public Collection<String> getHints();

}
