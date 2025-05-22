package com.threeamigos.pixelpeeper.implementations.datamodel;

import com.threeamigos.pixelpeeper.data.ExifMap;
import com.threeamigos.pixelpeeper.data.ExifTag;
import com.threeamigos.pixelpeeper.data.ExifValue;
import com.threeamigos.pixelpeeper.interfaces.datamodel.ExifCache;
import com.threeamigos.pixelpeeper.interfaces.datamodel.NamePattern;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.NamePatternPreferences;

import java.io.File;
import java.util.Optional;

public class NamePatternImpl implements NamePattern {

    private final NamePatternPreferences namePatternPreferences;
    private final ExifCache exifCache;

    public NamePatternImpl(NamePatternPreferences namePatternPreferences, ExifCache exifCache) {
        this.namePatternPreferences = namePatternPreferences;
        this.exifCache = exifCache;
    }

    @Override
    public boolean rename(File file) {
        Optional<ExifMap> exifMapOpt = exifCache.getExifMap(file);
        if (exifMapOpt.isPresent()) {
            String filename = file.getName();
            String extension;
            int idx = filename.lastIndexOf('.');
            if (idx >= 0) {
                extension = filename.substring(idx);
                filename = filename.substring(0, idx);
            } else {
                extension = "";
            }
            ExifMap exifMap = exifMapOpt.get();
            String pattern = namePatternPreferences.getNamePattern();
            pattern = pattern.replaceAll("\\" + LEFT_BRACKET + "FILENAME" + RIGHT_BRACKET, filename);
            for (ExifTag exifTag : ExifTag.values()) {
                String occurrence = "\\" + LEFT_BRACKET + exifTag.name() + RIGHT_BRACKET;
                ExifValue value = exifMap.getExifValue(exifTag);
                if (value != null) {
                    pattern = pattern.replaceAll(occurrence, exifMap.getTagDescriptive(exifTag));
                }
            }
            pattern = pattern.replaceAll("[fF]/", "F").replace("/", "_");
            filename = pattern + extension;
            File newFile = new File(file.getPath().substring(0, file.getPath().length() - file.getName().length()) + File.separatorChar + filename);
            if (!newFile.exists()) {
                return file.renameTo(newFile);
            }
        }
        return false;
    }

}
