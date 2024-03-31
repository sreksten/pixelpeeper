package com.threeamigos.pixelpeeper.implementations.datamodel;

import com.threeamigos.common.util.interfaces.ui.FontService;
import com.threeamigos.pixelpeeper.data.ExifTag;
import com.threeamigos.pixelpeeper.data.PictureData;
import com.threeamigos.pixelpeeper.interfaces.datamodel.TagsClassifier;
import com.threeamigos.pixelpeeper.interfaces.datamodel.TagsRenderer;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavours.ExifTagPreferences;

import java.awt.*;
import java.awt.image.BufferedImage;

import static java.awt.image.BufferedImage.TYPE_INT_ARGB;

public class TagsRenderShadowHelper extends AbstractTagsRenderer implements TagsRenderer {

    private static final int MULTIPLIER = 2;
    private static final int HSPACING = 5;
    private static final int VSPACING = 5;

    private static final int FONT_HEIGHT = 16;

    private final FontService fontService;
    private final PictureData pictureData;
    private final ExifTagPreferences tagPreferences;
    private final TagsClassifier commonTagsHelper;

    private Image image;
    private int realY;
    private Graphics2D g2d;
    private int x;
    private int y;

    TagsRenderShadowHelper(FontService fontService, PictureData pictureData,
                           ExifTagPreferences tagPreferences, TagsClassifier commonTagsHelper) {
        super(tagPreferences, commonTagsHelper);
        this.fontService = fontService;
        this.pictureData = pictureData;
        this.tagPreferences = tagPreferences;
        this.commonTagsHelper = commonTagsHelper;
    }

    public void reset() {
        this.image = null;
    }

    public void render(Graphics g2d, int x, int y) {
        this.g2d = (Graphics2D) g2d;
        this.x = x + HSPACING;
        this.y = y - VSPACING - FONT_HEIGHT;

        if (tagPreferences.isTagsVisible()) {
            if (image == null) {
                image = buildImage();
                realY = y - image.getHeight(null);
            }
            g2d.drawImage(image, x, realY, null);
        }
    }

    private Image buildImage() {
        int textsWidth = calculateWidth(g2d);
        int textsHeight = calculateHeight();

        BufferedImage texts = createTextsImage(textsWidth, textsHeight);
        int[][] shadowMatrix = buildShadowMatrix(21, 21);
        final int shadowMatrixWidth = shadowMatrix[0].length;
        final int shadowMatrixHeight = shadowMatrix.length;

        Image shadows = createShadowsImage(texts, textsWidth, textsHeight, shadowMatrix);

        image = new BufferedImage(textsWidth + shadowMatrixWidth, textsHeight + shadowMatrixHeight, TYPE_INT_ARGB);
        Graphics gfx = image.getGraphics();
        gfx.drawImage(shadows, 0, 0, null);
        gfx.drawImage(texts, shadowMatrixWidth / 2, shadowMatrixHeight / 2, null);
        gfx.dispose();
        return image;
    }

    private BufferedImage createTextsImage(int width, int height) {
        BufferedImage textsImage = new BufferedImage(width, height, TYPE_INT_ARGB);
        Graphics2D graphics = textsImage.createGraphics();
        graphics.setColor(Color.WHITE);
        int y = FONT_HEIGHT * MULTIPLIER;
        graphics.setFont(getImageNameFont());
        graphics.drawString(pictureData.getFilename(), 0, y);
        graphics.setFont(getTagFont());
        for (ExifTag exifTag : tagsToCheck) {
            if (isVisible(exifTag)) {
                y += FONT_HEIGHT + VSPACING;
                String tagDescription = exifTag.getDescription();
                String tagValue = pictureData.getTagDescriptive(exifTag);
                Color color = commonTagsHelper.getTotalMappedPictures() == 1 || commonTagsHelper.isCommonTag(exifTag)
                        ? Color.GREEN
                        : Color.YELLOW;
                graphics.setColor(color);
                graphics.drawString(String.format("%s: %s", tagDescription, tagValue), 0, y);
            }
        }
        graphics.dispose();
        return textsImage;
    }

    private int[][] buildShadowMatrix(final int width, final int height) {
        final int centerX = width / 2;
        final int centerY = height / 2;
        final int radius = width / 2;

        int[] shadowIntensity = new int[radius];
        for (int i = 0; i < radius; i++) {
            shadowIntensity[i] = (int) (255.0f * (1 - (double) i / radius));
        }

        int[][] shadowMatrix = new int[height][width];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (x == centerX && y == centerY) {
                    shadowMatrix[y][x] = 0xFF;
                } else {
                    double distanceFromCenter = Math.sqrt(Math.pow(Math.abs(x - centerX), 2) + Math.pow(Math.abs(y - centerY), 2));
                    if (distanceFromCenter >= radius) {
                        shadowMatrix[y][x] = 0;
                    } else {
                        shadowMatrix[y][x] = shadowIntensity[(int) distanceFromCenter];
                    }
                }
            }
        }
        return shadowMatrix;
    }

    private Image createShadowsImage(BufferedImage texts, int textsWidth, int textsHeight, int[][] shadowMatrix) {
        final int matrixHeight = shadowMatrix.length;
        final int matrixWidth = shadowMatrix[0].length;
        final int width = textsWidth + matrixWidth;
        final int height = textsHeight + matrixHeight;
        int[] colors = new int[width * height];
        int[] alpha = new int[width * height];
        int[] textsPixels = new int[textsWidth * textsHeight];
        texts.getRGB(0, 0, textsWidth, textsHeight, textsPixels, 0, textsWidth);

        for (int x = 0; x < textsWidth; x++) {
            for (int y = 0; y < textsHeight; y++) {
                int pixel = textsPixels[x + y * textsWidth];
                if (pixel != 0) {
                    for (int i = 0; i < matrixWidth; i++) {
                        for (int j = 0; j < matrixHeight; j++) {
                            int newAlpha = shadowMatrix[j][i];
                            if (newAlpha > 0) {
                                final int cx = x + i;
                                final int cy = y + j;
                                final int index = cx + cy * width;
                                int presentAlpha = alpha[index];
                                if (presentAlpha < newAlpha) {
                                    alpha[index] = newAlpha;
                                    colors[index] = (newAlpha << 24) + 0x010101;
                                }
                            }
                        }
                    }
                }
            }
        }

        BufferedImage shadows = new BufferedImage(width, height, TYPE_INT_ARGB);
        shadows.setRGB(0, 0, width, height, colors, 0, width);
        return shadows;
    }

    private int calculateWidth(Graphics g) {
        int width = g.getFontMetrics(getImageNameFont()).stringWidth(pictureData.getFilename());
        for (ExifTag exifTag : tagsToCheck) {
            if (isVisible(exifTag)) {
                int tagWidth = g.getFontMetrics(getTagFont()).stringWidth(pictureData.getFilename());
                width = Math.max(width, tagWidth);
            }
        }
        return width;
    }

    private int calculateHeight() {
        int height = FONT_HEIGHT * MULTIPLIER;
        height += (VSPACING + FONT_HEIGHT) * getVisibleTagsCount();
        return height;
    }

    private int getVisibleTagsCount() {
        int visibleTags = 0;
        for (ExifTag exifTag : tagsToCheck) {
            if (isVisible(exifTag)) {
                visibleTags++;
            }
        }
        return visibleTags;
    }

    private Font getImageNameFont() {
        return fontService.getFont("Arial", Font.BOLD, FONT_HEIGHT * MULTIPLIER);
    }

    private Font getTagFont() {
        return fontService.getFont("Arial", Font.BOLD, FONT_HEIGHT);
    }
}
