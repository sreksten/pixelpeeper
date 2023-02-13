package com.threeamigos.pixelpeeper.interfaces.ui;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;

public interface DragAndDropWindow extends Consumer<List<File>>, HintsProducer {

	public void setVisible(boolean visible);

	public void setProxyFor(ImageConsumer consumer);

}
