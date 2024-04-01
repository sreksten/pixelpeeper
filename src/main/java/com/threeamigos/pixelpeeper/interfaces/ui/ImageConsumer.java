package com.threeamigos.pixelpeeper.interfaces.ui;

import com.threeamigos.pixelpeeper.data.ExifTag;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;

public interface ImageConsumer extends Consumer<List<File>> {

    void accept(List<File> files, ExifTag tagToGroupBy, int tolerance, ExifTag tagToOrderBy);

}
