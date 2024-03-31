package com.threeamigos.pixelpeeper.implementations.datamodel;

import com.threeamigos.common.util.interfaces.ui.FontService;
import com.threeamigos.common.util.ui.effects.text.BorderedStringRenderer;
import com.threeamigos.pixelpeeper.data.ExifTag;
import com.threeamigos.pixelpeeper.data.PictureData;
import com.threeamigos.pixelpeeper.interfaces.datamodel.TagsClassifier;
import com.threeamigos.pixelpeeper.interfaces.datamodel.TagsRenderer;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavours.ExifTagPreferences;

import java.awt.*;

public class TagsRendererSimpleBorder extends AbstractTagsRenderer implements TagsRenderer {

    private static final int HSPACING = 5;
    private static final int VSPACING = 5;

    private static final int FONT_HEIGHT = 16;

    private final FontService fontService;
    private final PictureData pictureData;

    TagsRendererSimpleBorder(FontService fontService, PictureData pictureData,
                             ExifTagPreferences tagPreferences, TagsClassifier commonTagsHelper) {
        super(tagPreferences, commonTagsHelper);
        this.fontService = fontService;
        this.pictureData = pictureData;
    }

    public void reset() {
        // No implementation
    }

    public void render(Graphics g, int x, int y) {
        Graphics2D g2d = (Graphics2D) g;
        x = x + HSPACING;
        y = y - VSPACING - FONT_HEIGHT;

        if (tagPreferences.isTagsVisible()) {
            Font smallerFont = fontService.getFont("Arial", Font.BOLD, FONT_HEIGHT);
            g2d.setFont(smallerFont);
            for (ExifTag exifTag : tagsToCheck) {
                if (isVisible(exifTag)) {
                    info(exifTag, g2d, x, y);
                    y -= FONT_HEIGHT + VSPACING;
                }
            }
        }

        Font font = fontService.getFont("Arial", Font.BOLD, FONT_HEIGHT * 2);
        g2d.setFont(font);
        BorderedStringRenderer.drawString(g2d, pictureData.getFilename(), x, y, Color.BLACK, Color.WHITE);
    }

    private void info(ExifTag exifTag, Graphics2D g2d, final int x, final int y) {
        String tagDescription = exifTag.getDescription();
        String tagValue = pictureData.getTagDescriptive(exifTag);
        BorderedStringRenderer.drawString(g2d, String.format("%s: %s", tagDescription, tagValue), x, y,
                Color.BLACK,
                (commonTagsHelper.getTotalMappedPictures() == 1 || commonTagsHelper.isCommonTag(exifTag))
                        ? Color.GREEN
                        : Color.YELLOW);
    }
}
