package com.threeamigos.pixelpeeper.implementations.datamodel;

import com.threeamigos.common.util.interfaces.ui.FontService;
import com.threeamigos.common.util.ui.effects.text.BorderedStringRenderer;
import com.threeamigos.pixelpeeper.data.PictureData;
import com.threeamigos.pixelpeeper.implementations.helpers.ImageDrawHelper;
import com.threeamigos.pixelpeeper.interfaces.datamodel.CommunicationMessages;
import com.threeamigos.pixelpeeper.interfaces.datamodel.ExifTagsClassifier;
import com.threeamigos.pixelpeeper.interfaces.datamodel.ImageSlice;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavours.DrawingPreferences;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavours.EdgesDetectorPreferences;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavours.ImageHandlingPreferences;
import com.threeamigos.pixelpeeper.interfaces.ui.InfoRenderer;
import com.threeamigos.pixelpeeper.interfaces.ui.InfoRendererFactory;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

public class ImageSliceImpl implements ImageSlice, PropertyChangeListener {

    private final PictureData pictureData;
    private final ExifTagsClassifier exifTagsClassifier;
    private final ImageHandlingPreferences imageHandlingPreferences;
    private final DrawingPreferences drawingPreferences;
    private final EdgesDetectorPreferences edgesDetectorPreferences;
    private final FontService fontService;
    private final InfoRendererFactory infoRendererFactory;

    private final PropertyChangeSupport propertyChangeSupport;
    private InfoRenderer infoRenderer;

    private Rectangle location;
    private double imageOffsetX;
    private double imageOffsetY;

    private boolean selected;

    private boolean isDrawing;
    private Doodle currentDrawing;
    private final List<Doodle> doodles;

    private boolean edgeCalculationInProgress;

    public ImageSliceImpl(PictureData pictureData, ExifTagsClassifier exifTagsClassifier, InfoRendererFactory infoRendererFactory,
                          ImageHandlingPreferences imageHandlingPreferences, DrawingPreferences drawingPreferences,
                          EdgesDetectorPreferences edgesDetectorPreferences, FontService fontService) {
        this.pictureData = pictureData;
        this.exifTagsClassifier = exifTagsClassifier;
        pictureData.addPropertyChangeListener(this);
        this.imageHandlingPreferences = imageHandlingPreferences;
        this.drawingPreferences = drawingPreferences;
        this.edgesDetectorPreferences = edgesDetectorPreferences;
        this.fontService = fontService;
        this.infoRendererFactory = infoRendererFactory;

        currentDrawing = new Doodle();
        doodles = new ArrayList<>();

        propertyChangeSupport = new PropertyChangeSupport(this);
    }

    @Override
    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    public void setLocation(Rectangle location) {
        this.location = location;
        imageOffsetX = (double) (pictureData.getWidth() - location.width) / 2;
        imageOffsetY = (double) (pictureData.getHeight() - location.height) / 2;
        checkBoundaries();
    }

    @Override
    public void move(double deltaX, double deltaY) {
        imageOffsetX += deltaX;
        imageOffsetY += deltaY;
        checkBoundaries();
    }

    @Override
    public void startDoodling() {
        isDrawing = true;
        currentDrawing = new Doodle();
        doodles.add(currentDrawing);
    }

    @Override
    public void addVertex(int x, int y) {
        currentDrawing.addVertex(x, y);
    }

    @Override
    public void stopDoodling() {
        isDrawing = false;
    }

    @Override
    public void undoLastDoodle() {
        if (!doodles.isEmpty()) {
            doodles.remove(doodles.size() - 1);
        }
        if (isDrawing) {
            startDoodling();
        }
    }

    @Override
    public void clearDoodles() {
        doodles.clear();
        if (isDrawing) {
            startDoodling();
        }
    }

    private void checkBoundaries() {
        int pictureWidth = pictureData.getWidth();
        if (location != null && imageOffsetX > pictureWidth - location.width) {
            imageOffsetX = (double) pictureWidth - location.width;
        }
        if (imageOffsetX < 0.0d) {
            imageOffsetX = 0.0d;
        }

        int pictureHeight = pictureData.getHeight();
        if (location != null && imageOffsetY > pictureHeight - location.height) {
            imageOffsetY = (double) pictureHeight - location.height;
        }
        if (imageOffsetY < 0.0d) {
            imageOffsetY = 0.0d;
        }
    }

    @Override
    public void resetMovement() {
        if (location != null) {
            imageOffsetX = (double) (pictureData.getWidth() - location.width) / 2;
            if (imageOffsetX < 0.0d) {
                imageOffsetX = 0.0d;
            }
            imageOffsetY = (double) (pictureData.getHeight() - location.height) / 2;
            if (imageOffsetY < 0.0d) {
                imageOffsetY = 0.0d;
            }
        } else {
            imageOffsetX = 0.0d;
            imageOffsetY = 0.0d;
        }
    }

    @Override
    public void changeZoomLevel(float zoomLevel) {

        if (location == null) {
            return;
        }

        int futurePictureWidth = (int) (pictureData.getOriginalWidth() * zoomLevel
                / ImageHandlingPreferences.MAX_ZOOM_LEVEL);
        if (futurePictureWidth < location.width) {
            imageOffsetX = 0;
        } else {
            if (pictureData.getWidth() < location.width) {
                imageOffsetX = (double) (futurePictureWidth - location.width) / 2;
            } else {
                double centerXPercentage = (imageOffsetX + (double) location.width / 2) / pictureData.getWidth();
                double futureCenterX = centerXPercentage * futurePictureWidth;
                imageOffsetX = futureCenterX - (double) location.width / 2;
            }
        }
        int futurePictureHeight = (int) (pictureData.getOriginalHeight() * zoomLevel
                / ImageHandlingPreferences.MAX_ZOOM_LEVEL);
        if (futurePictureHeight < location.height) {
            imageOffsetY = 0;
        } else {
            if (pictureData.getHeight() < location.height) {
                imageOffsetY = (double) (futurePictureHeight - location.height) / 2;
            } else {
                double centerYPercentage = (imageOffsetY + (double) location.height / 2) / pictureData.getHeight();
                double futureCenterY = centerYPercentage * futurePictureHeight;
                imageOffsetY = futureCenterY - (double) location.height / 2;
            }
        }

        pictureData.changeZoomLevel(zoomLevel);

        checkBoundaries();
    }

    @Override
    public float getZoomLevel() {
        return pictureData.getZoomLevel();
    }

    @Override
    public Rectangle getLocation() {
        return location;
    }

    @Override
    public boolean contains(int x, int y) {
        return location != null && location.x <= x && x < location.x + location.width && location.y <= y
                && y < location.y + location.height;
    }

    @Override
    public PictureData getPictureData() {
        return pictureData;
    }

    @Override
    public void paint(Graphics2D g2d) {

        if (location == null) {
            return;
        }

        int locationX = location.x;
        int locationY = location.y;
        int locationWidth = location.width;
        int locationHeight = location.height;
        int pictureWidth = pictureData.getWidth();
        int pictureHeight = pictureData.getHeight();

        if (pictureWidth < locationWidth || pictureHeight < locationHeight) {
            g2d.setColor(Color.GRAY);
            g2d.drawRect(locationX, locationY, locationWidth - 1, locationHeight - 1);
        }

        int imageSliceWidth = Math.min(locationWidth, pictureWidth);
        int imageSliceStartX = (int) imageOffsetX;
        if (imageSliceStartX < 0) {
            imageSliceStartX = 0;
        }
        if (imageSliceStartX + imageSliceWidth > pictureWidth) {
            imageSliceStartX = pictureWidth - imageSliceWidth;
        }

        int imageSliceHeight = Math.min(locationHeight, pictureHeight);
        int imageSliceStartY = (int) imageOffsetY;
        if (imageSliceStartY < 0) {
            imageSliceStartY = 0;
        }
        if (imageSliceStartY + imageSliceHeight > pictureHeight) {
            imageSliceStartY = pictureHeight - imageSliceHeight;
        }

        BufferedImage subImage = pictureData.getImage().getSubimage(imageSliceStartX, imageSliceStartY, imageSliceWidth,
                imageSliceHeight);
        BufferedImage edgesImage = null;

        if (edgesDetectorPreferences.isShowEdges()) {
            edgesImage = pictureData.getEdgesImage();
            if (edgesImage != null) {
                edgesImage = edgesImage.getSubimage(imageSliceStartX, imageSliceStartY, imageSliceWidth,
                        imageSliceHeight);
            }
        }

        Shape previousClip = g2d.getClip();

        g2d.setClip(locationX, locationY, locationWidth, locationHeight);

        int pictureX = locationX;
        int zoomOffsetX = 0;
        if (locationWidth > pictureWidth) {
            zoomOffsetX = (locationWidth - pictureWidth) / 2;
            pictureX += zoomOffsetX;
        }
        int pictureY = locationY;
        int zoomOffsetY = 0;
        if (locationHeight > pictureHeight) {
            zoomOffsetY = (locationHeight - pictureHeight) / 2;
            pictureY += zoomOffsetY;
        }

        drawDetectedEdges(g2d, edgesImage, subImage, pictureX, pictureY);
        drawSelectedRectangle(g2d, locationX, locationY, locationWidth, locationHeight);
        drawDoodles(g2d, zoomOffsetX, zoomOffsetY);
        drawMiniatureWithPosition(g2d);
        drawInfos(g2d, locationX, locationY, locationHeight);
        drawEdgeCalculationInProgessNotice(g2d, locationX, locationY);

        g2d.setClip(previousClip);
    }

    private void drawDetectedEdges(Graphics2D g2d, BufferedImage edgesImage, BufferedImage subImage, int pictureX, int pictureY) {
        if (!edgesDetectorPreferences.isShowEdges()
                || edgesDetectorPreferences.getEdgesTransparency() == EdgesDetectorPreferences.TOTAL_EDGES_TRANSPARENCY
                || edgesImage == null) {
            g2d.drawImage(subImage, pictureX, pictureY, null);
        } else if (edgesDetectorPreferences.getEdgesTransparency() == EdgesDetectorPreferences.NO_EDGES_TRANSPARENCY) {
            g2d.drawImage(edgesImage, pictureX, pictureY, null);
        } else {
            ImageDrawHelper.drawTransparentImageAtop(g2d, subImage, edgesImage, pictureX, pictureY,
                    edgesDetectorPreferences.getEdgesTransparency());
        }
    }

    private void drawSelectedRectangle(Graphics2D g2d, int locationX, int locationY, int locationWidth, int locationHeight) {
        if (selected) {
            g2d.setColor(Color.RED);
            g2d.drawRect(locationX, locationY, locationWidth - 1, locationHeight - 1);
        }
    }

    private void drawEdgeCalculationInProgessNotice(Graphics2D g2d, int locationX, int locationY) {
        if (edgeCalculationInProgress) {
            Font font = fontService.getFont("Arial", Font.BOLD, 24);
            g2d.setFont(font);
            BorderedStringRenderer.drawString(g2d, "Edge calculation in progress", locationX + 10, locationY + 30,
                    Color.BLACK, Color.WHITE);
        }
    }

    private void drawInfos(Graphics2D g2d, int locationX, int locationY, int locationHeight) {
        if (infoRenderer == null) {
            infoRenderer = infoRendererFactory.getInfoRenderer(pictureData, exifTagsClassifier);
        }
        infoRenderer.render(g2d, locationX, locationY + locationHeight - 1);
    }

    private void drawDoodles(Graphics2D g2d, int zoomOffsetX, int zoomOffsetY) {
        for (Doodle doodle : doodles) {
            doodle.paint(g2d, zoomOffsetX, zoomOffsetY);
        }
    }

    private void drawMiniatureWithPosition(Graphics2D g2d) {
        int locationX = location.x;
        int locationY = location.y;
        int locationWidth = location.width;
        int locationHeight = location.height;
        int pictureWidth = pictureData.getWidth();
        int pictureHeight = pictureData.getHeight();

        if (imageHandlingPreferences.isPositionMiniatureVisible()
                && (pictureWidth > locationWidth || pictureHeight > locationHeight)) {

            Shape previousClip = g2d.getClip();

            int miniatureWidth = locationWidth / 5;
            int miniatureHeight = miniatureWidth * pictureHeight / pictureWidth;

            int miniatureX = locationX + locationWidth - 1 - miniatureWidth - locationWidth / 20;
            int miniatureY = locationY + locationHeight - 1 - miniatureHeight - locationWidth / 20;

            g2d.setColor(Color.DARK_GRAY);
            drawFilledRectangle(g2d, miniatureX, miniatureY, miniatureWidth, miniatureHeight, 0);

            g2d.setColor(Color.GRAY);
            int visibleWidth = locationWidth * miniatureWidth / pictureWidth;
            if (visibleWidth > miniatureWidth) {
                visibleWidth = miniatureWidth;
            }
            int visibleHeight = locationHeight * miniatureHeight / pictureHeight;
            if (visibleHeight > miniatureHeight) {
                visibleHeight = miniatureHeight;
            }

            int screenXOffsetScaled = pictureWidth > locationWidth
                    ? (int) (imageOffsetX * miniatureWidth / pictureWidth)
                    : 0;
            int screenYOffsetScaled = pictureHeight > locationHeight
                    ? (int) (imageOffsetY * miniatureHeight / pictureHeight)
                    : 0;

            drawFilledRectangle(g2d, miniatureX + screenXOffsetScaled, miniatureY + screenYOffsetScaled, visibleWidth,
                    visibleHeight, screenYOffsetScaled);

            g2d.setClip(previousClip);
        }
    }

    private void drawFilledRectangle(Graphics2D g2d, int x, int y, int width, int height, int offset) {
        g2d.setClip(x, y, width + 1, height + 1);
        g2d.drawRect(x, y, width, height);

        final int limit = x + width + height;
        final int spacing = 8;

        x = ((x >> 4) << 4) - offset % spacing;

        for (int i = x; i < limit; i += spacing) {
            g2d.drawLine(i, y, i - height, y + height);
        }
    }

    @Override
    public void adjustRotation(boolean autorotation) {
        if (autorotation) {
            pictureData.correctOrientation();
        } else {
            pictureData.undoOrientationCorrection();
        }
    }

    @Override
    public void startEdgesCalculation() {
        pictureData.startEdgesCalculation();
    }

    @Override
    public void releaseEdges() {
        pictureData.releaseEdges();
        edgeCalculationInProgress = false;
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        propertyChangeSupport.addPropertyChangeListener(pcl);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        propertyChangeSupport.removePropertyChangeListener(pcl);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (CommunicationMessages.EDGES_CALCULATION_STARTED.equals(evt.getPropertyName())) {
            handleEdgeCalculationStarted();
        } else if (CommunicationMessages.EDGES_CALCULATION_COMPLETED.equals(evt.getPropertyName())) {
            handleEdgeCalculationCompleted();
        } else if (CommunicationMessages.TAG_VISIBILITY_CHANGED.equals(evt.getPropertyName()) ||
                CommunicationMessages.TAGS_VISIBILITY_CHANGED.equals(evt.getPropertyName()) ||
                CommunicationMessages.TAGS_VISIBILITY_OVERRIDE_CHANGED.equals(evt.getPropertyName())) {
            infoRenderer.reset();
        } else if (CommunicationMessages.TAGS_RENDERING_CHANGED.equals(evt.getPropertyName())) {
            infoRenderer = null;
        }
    }

    private void handleEdgeCalculationStarted() {
        edgeCalculationInProgress = true;
        propertyChangeSupport.firePropertyChange(CommunicationMessages.EDGES_CALCULATION_STARTED, null, this);
    }

    private void handleEdgeCalculationCompleted() {
        edgeCalculationInProgress = false;
        propertyChangeSupport.firePropertyChange(CommunicationMessages.EDGES_CALCULATION_COMPLETED, null, this);
    }

    private class Doodle {
        private final Color color;
        private final int brushSize;
        private final int transparency;
        private final List<Point> points;

        Doodle() {
            color = drawingPreferences.getColor();
            brushSize = drawingPreferences.getBrushSize();
            transparency = drawingPreferences.getTransparency();
            points = new ArrayList<>();
        }

        void addVertex(int x, int y) {
            x = x - getScreenCenteringX();
            y = y - getScreenCenteringY();
            int pointX = x - location.x + (int) imageOffsetX;
            int pointY = y - location.y + (int) imageOffsetY;
            pointX = (int) (pointX / pictureData.getZoomLevel() * 100);
            pointY = (int) (pointY / pictureData.getZoomLevel() * 100);

            points.add(new Point(pointX, pointY));
        }

        public void paint(Graphics2D g2d, int zoomOffsetX, int zoomOffsetY) {
            Color previousColor = g2d.getColor();
            Stroke previousStroke = g2d.getStroke();
            Composite previousComposite = g2d.getComposite();
            g2d.setColor(color);
            g2d.setStroke(new BasicStroke(adapt(brushSize)));
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (100 - transparency) / 100.0f));
            Point lastPoint = null;
            int xOffset = zoomOffsetX - (int) imageOffsetX + location.x;
            int yOffset = zoomOffsetY - (int) imageOffsetY + location.y;
            for (Point point : points) {
                if (lastPoint != null) {
                    g2d.drawLine(adapt(lastPoint.x) + xOffset, adapt(lastPoint.y) + yOffset, adapt(point.x) + xOffset,
                            adapt(point.y) + yOffset);
                }
                lastPoint = point;
            }
            g2d.setColor(previousColor);
            g2d.setStroke(previousStroke);
            g2d.setComposite(previousComposite);
        }

        private int adapt(int coordinate) {
            return (int) (coordinate * getZoomLevel() / ImageHandlingPreferences.MAX_ZOOM_LEVEL);
        }

        private int getScreenCenteringX() {
            int screenCenteringX = 0;
            int locationWidth = location.width;
            int pictureWidth = pictureData.getWidth();
            if (locationWidth > pictureWidth) {
                screenCenteringX = (locationWidth - pictureWidth) / 2;
            }
            return screenCenteringX;
        }

        private int getScreenCenteringY() {
            int screenCenteringY = 0;
            int locationHeight = location.height;
            int pictureHeight = pictureData.getHeight();
            if (locationHeight > pictureHeight) {
                screenCenteringY = (locationHeight - pictureHeight) / 2;
            }
            return screenCenteringY;
        }

    }

}
