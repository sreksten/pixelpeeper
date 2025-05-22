package com.threeamigos.pixelpeeper.interfaces.preferences.flavors;

import com.threeamigos.common.util.interfaces.preferences.Preferences;
import com.threeamigos.pixelpeeper.data.ExifTag;

import java.util.List;

/**
 * Not exactly a set of preferences; the session keeps track of the
 * last directory browsed and the files that were loaded last.
 *
 * @author Stefano Reksten
 */
public interface SessionPreferences extends Preferences {

    default String getDescription() {
        return "Session preferences";
    }

    void setLastPath(String path);

    String getLastPath();

    void setLastFilenames(List<String> lastFilenames);

    List<String> getLastFilenames();

    void setTagToGroupBy(ExifTag exifTag);

    ExifTag getTagToGroupBy();

    void setTolerance(int tolerance);

    int getTolerance();

    void setTagToOrderBy(ExifTag exifTag);

    ExifTag getTagToOrderBy();

    void setGroupIndex(int lastGroup);

    int getGroupIndex();

}
