package com.threeamigos.pixelpeeper.interfaces.ui;

import com.threeamigos.pixelpeeper.data.ExifTag;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;

/**
 * An interface that is capable of accepting a list of files.<br/>
 * Other than the single parameter accept method, this interface can also accept a list of files
 * and then group and order the files, given an {@link ExifTag} to group by along with a given tolerance,
 * and another EXIF tag to sort them within the same group. This to provide the end-user with a simple
 * method to group images by lens, or manufacturer, and check the differences.<br>
 * For more information about the group and order capabilities see the {@link ExifTagsFilter}.
 *
 * @author Stefano Reksten
 */
public interface ImageConsumer extends Consumer<List<File>> {

    void accept(List<File> files, ExifTag tagToGroupBy, int tolerance, ExifTag tagToOrderBy);

}
