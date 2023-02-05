package com.threeamigos.imageviewer.interfaces.ui;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;

import com.threeamigos.imageviewer.data.ExifTag;

public interface ImageConsumer extends Consumer<List<File>> {

	public void accept(List<File> files, ExifTag tagToGroupBy, int tolerance);

}
