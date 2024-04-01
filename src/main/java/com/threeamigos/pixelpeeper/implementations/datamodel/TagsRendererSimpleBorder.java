package com.threeamigos.pixelpeeper.implementations.datamodel;

import com.threeamigos.common.util.interfaces.ui.FontService;
import com.threeamigos.common.util.ui.effects.text.BorderedStringRenderer;
import com.threeamigos.pixelpeeper.data.ExifTag;
import com.threeamigos.pixelpeeper.data.PictureData;
import com.threeamigos.pixelpeeper.interfaces.datamodel.TagsClassifier;
import com.threeamigos.pixelpeeper.interfaces.datamodel.TagsRenderer;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavours.ExifTagPreferences;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;

public class TagsRendererSimpleBorder extends AbstractTagsRenderer implements TagsRenderer {

    TagsRendererSimpleBorder(FontService fontService, PictureData pictureData,
                             ExifTagPreferences tagPreferences, TagsClassifier commonTagsHelper) {
        super(tagPreferences, commonTagsHelper, fontService, pictureData);
    }

    public void reset() {
        // No implementation
    }

    public void render(Graphics g, int x, int y) {
        Graphics2D g2d = (Graphics2D) g;
        x = x + HSPACING;
        y = y - VSPACING - FILENAME_FONT_HEIGHT;

        if (tagPreferences.isTagsVisible()) {
            g2d.setFont(getTagFont());
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
        BorderedStringRenderer.drawString(g2d, getCompleteTag(exifTag), x, y, Color.BLACK, getTagColor(exifTag));
    }
}
