package com.threeamigos.imageviewer.interfaces.ui;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;

public interface DragAndDropWindow extends Consumer<List<File>> {

	public void setVisible(boolean visible);

	public void setProxyFor(Consumer<List<File>> consumer);

}
