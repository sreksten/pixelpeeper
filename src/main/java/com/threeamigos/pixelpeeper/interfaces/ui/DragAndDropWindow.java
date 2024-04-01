package com.threeamigos.pixelpeeper.interfaces.ui;

import com.threeamigos.common.util.interfaces.ui.HintsProducer;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;

public interface DragAndDropWindow extends Consumer<List<File>>, HintsProducer<String> {

    void setVisible(boolean visible);

    void setProxyFor(ImageConsumer consumer);

}
