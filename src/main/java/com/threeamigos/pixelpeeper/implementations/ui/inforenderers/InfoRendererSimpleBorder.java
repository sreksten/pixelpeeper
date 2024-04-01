package com.threeamigos.pixelpeeper.implementations.ui.inforenderers;

import com.threeamigos.common.util.interfaces.ui.FontService;
import com.threeamigos.common.util.ui.effects.text.BorderedStringRenderer;
import com.threeamigos.pixelpeeper.data.ExifTag;
import com.threeamigos.pixelpeeper.data.PictureData;
import com.threeamigos.pixelpeeper.interfaces.datamodel.ExifTagsClassifier;
import com.threeamigos.pixelpeeper.interfaces.datamodel.InfoRenderer;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavours.ExifTagsPreferences;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;

public class InfoRendererSimpleBorder extends AbstractInfoRenderer implements InfoRenderer {

    InfoRendererSimpleBorder(FontService fontService, PictureData pictureData,
                             ExifTagsPreferences tagPreferences, ExifTagsClassifier commonTagsHelper) {
        super(tagPreferences, commonTagsHelper, fontService, pictureData);
    }

    public void reset() {
        // No implementation
    }

    public void render(Graphics g, int x, int y) {
        Graphics2D g2d = (Graphics2D) g;
        x = x + HSPACING;
        y = y - VSPACING - FILENAME_FONT_HEIGHT;

        if (exifTagsPreferences.isTagsVisible()) {
            g2d.setFont(getExifTagFont());
            List<ExifTag> exifTags = new LinkedList<>();
            for (ExifTag exifTag : tagsToCheck) {
                exifTags.add(0, exifTag);
            }
            for (ExifTag exifTag : exifTags) {
                if (isVisible(exifTag)) {
                    info(exifTag, g2d, x, y);
                    y -= TAG_FONT_HEIGHT + VSPACING;
                }
            }
        }

        g2d.setFont(getFilenameFont());
        BorderedStringRenderer.drawString(g2d, pictureData.getFilename(), x, y, Color.BLACK, getFilenameColor());
    }

    private void info(ExifTag exifTag, Graphics2D g2d, final int x, final int y) {
        BorderedStringRenderer.drawString(g2d, getCompleteTag(exifTag), x, y, Color.BLACK, getExifTagColor(exifTag));
    }
}
