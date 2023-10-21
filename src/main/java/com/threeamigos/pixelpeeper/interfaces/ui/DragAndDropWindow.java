package com.threeamigos.pixelpeeper.interfaces.ui;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;

import com.threeamigos.common.util.interfaces.ui.HintsProducer;

public interface DragAndDropWindow extends Consumer<List<File>>, HintsProducer<String> {

	public void setVisible(boolean visible);

	public void setProxyFor(ImageConsumer consumer);

}
