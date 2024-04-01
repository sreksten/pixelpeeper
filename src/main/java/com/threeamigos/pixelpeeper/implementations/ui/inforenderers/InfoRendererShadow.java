package com.threeamigos.pixelpeeper.implementations.ui.inforenderers;

import com.threeamigos.common.util.interfaces.ui.FontService;
import com.threeamigos.pixelpeeper.data.ExifTag;
import com.threeamigos.pixelpeeper.data.PictureData;
import com.threeamigos.pixelpeeper.interfaces.datamodel.ExifTagsClassifier;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavours.ExifTagsPreferences;
import com.threeamigos.pixelpeeper.interfaces.ui.InfoRenderer;

import java.awt.*;
import java.awt.image.BufferedImage;

import static java.awt.image.BufferedImage.TYPE_INT_ARGB;

public class InfoRendererShadow extends AbstractInfoRenderer implements InfoRenderer {

    private Image image;
    private int realY;
    private Graphics2D g2d;

    public InfoRendererShadow(FontService fontService, PictureData pictureData,
                              ExifTagsPreferences tagPreferences, ExifTagsClassifier exifTagsClassifier) {
        super(tagPreferences, exifTagsClassifier, fontService, pictureData);
    }

    public void reset() {
        this.image = null;
    }

    public void render(Graphics g2d, int x, int y) {
        this.g2d = (Graphics2D) g2d;

        if (image == null) {
            image = buildImage();
            realY = y - image.getHeight(null);
        }
        g2d.drawImage(image, x, realY, null);
    }

    private Image buildImage() {
        int textsWidth = calculateWidth(g2d);
        int textsHeight = calculateHeight();

        BufferedImage texts = createTextsImage(textsWidth, textsHeight);
        int borderThickness = exifTagsPreferences.getBorderThickness();
        short[][] shadowMatrix = buildShadowMatrix(borderThickness, borderThickness);
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
        graphics.setColor(getFilenameColor());
        int y = FILENAME_FONT_HEIGHT;
        graphics.setFont(getFilenameFont());
        graphics.drawString(pictureData.getFilename(), 0, y);
        graphics.setFont(getExifTagFont());
        for (ExifTag exifTag : tagsToCheck) {
            if (isVisible(exifTag)) {
                y += TAG_FONT_HEIGHT + VSPACING;
                graphics.setColor(getExifTagColor(exifTag));
                graphics.drawString(getCompleteTag(exifTag), 0, y);
            }
        }
        graphics.dispose();
        return textsImage;
    }

    private short[][] buildShadowMatrix(final int width, final int height) {
        final int centerX = width / 2;
        final int centerY = height / 2;
        final int radius = width / 2;

        short[] shadowIntensity = new short[radius];
        for (int i = 0; i < radius; i++) {
            shadowIntensity[i] = (short) (255.0f * (1 - (double) i / radius));
        }

        short[][] shadowMatrix = new short[height][width];
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

    private Image createShadowsImage(BufferedImage texts, int textsWidth, int textsHeight, short[][] shadowMatrix) {
        final int matrixHeight = shadowMatrix.length;
        final int matrixWidth = shadowMatrix[0].length;
        final int width = textsWidth + matrixWidth;
        final int height = textsHeight + matrixHeight;
        int[] colors = new int[width * height];
        short[] alpha = new short[width * height];
        int[] textsPixels = new int[textsWidth * textsHeight];
        texts.getRGB(0, 0, textsWidth, textsHeight, textsPixels, 0, textsWidth);

        for (int x = 0; x < textsWidth; x++) {
            for (int y = 0; y < textsHeight; y++) {
                int pixel = textsPixels[x + y * textsWidth];
                if (pixel != 0) {
                    for (int i = 0; i < matrixWidth; i++) {
                        for (int j = 0; j < matrixHeight; j++) {
                            short newAlpha = shadowMatrix[j][i];
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
        int width = g.getFontMetrics(getFilenameFont()).stringWidth(pictureData.getFilename());
        for (ExifTag exifTag : tagsToCheck) {
            if (isVisible(exifTag)) {
                int tagWidth = g.getFontMetrics(getExifTagFont()).stringWidth(getCompleteTag(exifTag));
                width = Math.max(width, tagWidth);
            }
        }
        return width;
    }

    private int calculateHeight() {
        float height = getFilenameFont().getLineMetrics(pictureData.getFilename(),
                g2d.getFontRenderContext()).getHeight();
        for (ExifTag exifTag : tagsToCheck) {
            if (isVisible(exifTag)) {
                height += VSPACING + TAG_FONT_HEIGHT;
            }
        }
        return (int) height;
    }
}
