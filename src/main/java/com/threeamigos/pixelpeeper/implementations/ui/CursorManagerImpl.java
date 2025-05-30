package com.threeamigos.pixelpeeper.implementations.ui;

import com.threeamigos.common.util.implementations.ui.StringHint;
import com.threeamigos.common.util.interfaces.ui.Hint;
import com.threeamigos.common.util.interfaces.ui.InputConsumer;
import com.threeamigos.pixelpeeper.interfaces.datamodel.CommunicationMessages;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.CursorPreferences;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.CursorPreferences.Rotation;
import com.threeamigos.pixelpeeper.interfaces.ui.CursorManager;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class CursorManagerImpl implements CursorManager, PropertyChangeListener {

    private static final Map<Integer, Rotation> keyCodesToRotation = new HashMap<>();

    static {
        keyCodesToRotation.put(KeyEvent.VK_NUMPAD1, Rotation.ROTATION_1);
        keyCodesToRotation.put(KeyEvent.VK_NUMPAD2, Rotation.ROTATION_2);
        keyCodesToRotation.put(KeyEvent.VK_NUMPAD3, Rotation.ROTATION_3);
        keyCodesToRotation.put(KeyEvent.VK_NUMPAD4, Rotation.ROTATION_4);
        keyCodesToRotation.put(KeyEvent.VK_NUMPAD6, Rotation.ROTATION_6);
        keyCodesToRotation.put(KeyEvent.VK_NUMPAD7, Rotation.ROTATION_7);
        keyCodesToRotation.put(KeyEvent.VK_NUMPAD8, Rotation.ROTATION_8);
        keyCodesToRotation.put(KeyEvent.VK_NUMPAD9, Rotation.ROTATION_9);
    }

    private final CursorPreferences pointerPreferences;

    private final PropertyChangeSupport propertyChangeSupport;

    private boolean dragging;
    private Cursor cursor;

    public CursorManagerImpl(CursorPreferences bigPointerPreferences) {
        this.pointerPreferences = bigPointerPreferences;

        propertyChangeSupport = new PropertyChangeSupport(this);

        updateCursor();
    }

    @Override
    public Cursor getCursor() {
        return cursor;
    }

    public InputConsumer getInputConsumer() {
        return new InputAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {

                if (e.getKeyCode() == KeyEvent.VK_NUMPAD5) {
                    pointerPreferences.setBigPointerVisible(!pointerPreferences.isBigPointerVisible());
                    updateCursor();

                } else if (pointerPreferences.isBigPointerVisible()) {
                    Rotation rotation = keyCodesToRotation.get(e.getKeyCode());
                    if (rotation != null) {
                        pointerPreferences.setBigPointerRotation(rotation);
                        updateCursor();
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                dragging = true;
                updateCursor();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                dragging = false;
                updateCursor();
            }
        };
    }

    private void updateCursor() {
        if (dragging) {
            if (pointerPreferences.isBigPointerVisible()) {
                cursor = createHandCursor();
            } else {
                cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
            }
        } else {
            if (pointerPreferences.isBigPointerVisible()) {
                cursor = createArrowCursor();
            } else {
                cursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
            }
        }
        propertyChangeSupport.firePropertyChange(CommunicationMessages.BIG_POINTER_IMAGE_UPDATE_REQUEST, null, this);
    }

    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        propertyChangeSupport.addPropertyChangeListener(pcl);
    }

    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        propertyChangeSupport.removePropertyChangeListener(pcl);
    }

    private Cursor createArrowCursor() {

        int size = pointerPreferences.getBigPointerSize();

        BufferedImage cursorImage = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = cursorImage.createGraphics();

        Point[] vertexes = new Point[7];

        // Draws a right-pointing arrow starting from the hotspot (arrow point)

        vertexes[0] = new Point(0, 0);
        vertexes[1] = new Point(size * -79 / 100, size * 30 / 100);
        vertexes[2] = new Point(size * -69 / 100, size * 10 / 100);
        vertexes[3] = new Point(-size, size * 10 / 100);
        vertexes[4] = new Point(-size, size * -10 / 100);
        vertexes[5] = new Point(size * -69 / 100, size * -10 / 100);
        vertexes[6] = new Point(size * -79 / 100, size * -30 / 100);

        graphics.setColor(Color.BLACK);

        drawPolygon(graphics, vertexes, size);

        vertexes[0] = new Point(size * -5 / 100, 0);
        vertexes[1] = new Point(size * -76 / 100, size * 27 / 100);
        vertexes[2] = new Point(size * -66 / 100, size * 8 / 100);
        vertexes[3] = new Point(size * -98 / 100, size * 8 / 100);
        vertexes[4] = new Point(size * -98 / 100, size * -8 / 100);
        vertexes[5] = new Point(size * -66 / 100, size * -8 / 100);
        vertexes[6] = new Point(size * -76 / 100, size * -27 / 100);

        graphics.setColor(Color.WHITE);

        drawPolygon(graphics, vertexes, size);

        graphics.dispose();

        int hotspotX = size / 2 + (int) ((double) size / 2 * Math.cos(pointerPreferences.getBigPointerRotation().getRadians()))
                - 1;
        if (hotspotX < 0) {
            hotspotX = 0;
        }
        int hotspotY = size / 2 + (int) ((double) size / 2 * Math.sin(pointerPreferences.getBigPointerRotation().getRadians()))
                - 1;
        if (hotspotY < 0) {
            hotspotY = 0;
        }

        return Toolkit.getDefaultToolkit().createCustomCursor(cursorImage, new Point(hotspotX, hotspotY), "bigPointer");
    }

    private void drawPolygon(Graphics2D graphics, Point[] vertexes, int size) {

        Polygon arrow = new Polygon();
        for (Point vertex : vertexes) {
            arrow.addPoint(size + (int) vertex.getX(), size / 2 + (int) vertex.getY());
        }

        AffineTransform transform = new AffineTransform();
        transform.rotate(pointerPreferences.getBigPointerRotation().getRadians(), (double) size / 2, (double) size / 2);
        graphics.fill(transform.createTransformedShape(arrow));
    }

    private Cursor createHandCursor() {

        int size = pointerPreferences.getBigPointerSize() - 1;

        BufferedImage cursorImage = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = cursorImage.createGraphics();

        Point[] vertexes = new Point[19];

        vertexes[0] = new Point(size * 18 / 100, size * 11 / 100);
        vertexes[1] = new Point(size * 25 / 100, size * 5 / 100);
        vertexes[2] = new Point(size * 33 / 100, size * 5 / 100);
        vertexes[3] = new Point(size * 37 / 100, size * 9 / 100);
        vertexes[4] = new Point(size * 40 / 100, size * 4 / 100);
        vertexes[5] = new Point(size * 47 / 100, size * 4 / 100);
        vertexes[6] = new Point(size * 51 / 100, size * 7 / 100);
        vertexes[7] = new Point(size * 54 / 100, size * 5 / 100);
        vertexes[8] = new Point(size * 62 / 100, size * 5 / 100);
        vertexes[9] = new Point(size * 66 / 100, size * 11 / 100);
        vertexes[10] = new Point(size * 69 / 100, size * 10 / 100);
        vertexes[11] = new Point(size * 75 / 100, size * 10 / 100);
        vertexes[12] = new Point(size * 85 / 100, size * 34 / 100);
        vertexes[13] = new Point(size * 85 / 100, size * 78 / 100);
        vertexes[14] = new Point(size * 70 / 100, size * 94 / 100);
        vertexes[15] = new Point(size * 18 / 100, size * 94 / 100);
        vertexes[16] = new Point(size * 6 / 100, size * 72 / 100);
        vertexes[17] = new Point(size * 8 / 100, size * 39 / 100);
        vertexes[18] = new Point(size * 18 / 100, size * 33 / 100);

        graphics.setColor(Color.BLACK);

        Polygon arrow = new Polygon();
        for (Point vertex : vertexes) {
            arrow.addPoint((int) vertex.getX(), (int) vertex.getY());
        }
        graphics.fill(arrow);

        vertexes[0] = new Point(size * 24 / 100, size * 14 / 100);
        vertexes[1] = new Point(size * 28 / 100, size * 10 / 100);
        vertexes[2] = new Point(size * 32 / 100, size * 10 / 100);
        vertexes[3] = new Point(size * 37 / 100, size * 14 / 100);
        vertexes[4] = new Point(size * 42 / 100, size * 10 / 100);
        vertexes[5] = new Point(size * 48 / 100, size * 10 / 100);
        vertexes[6] = new Point(size * 52 / 100, size * 15 / 100);
        vertexes[7] = new Point(size * 56 / 100, size * 10 / 100);
        vertexes[8] = new Point(size * 60 / 100, size * 10 / 100);
        vertexes[9] = new Point(size * 66 / 100, size * 18 / 100);
        vertexes[10] = new Point(size * 69 / 100, size * 16 / 100);
        vertexes[11] = new Point(size * 73 / 100, size * 16 / 100);
        vertexes[12] = new Point(size * 80 / 100, size * 36 / 100);
        vertexes[13] = new Point(size * 80 / 100, size * 75 / 100);
        vertexes[14] = new Point(size * 66 / 100, size * 89 / 100);
        vertexes[15] = new Point(size * 23 / 100, size * 89 / 100);
        vertexes[16] = new Point(size * 12 / 100, size * 69 / 100);
        vertexes[17] = new Point(size * 14 / 100, size * 42 / 100);
        vertexes[18] = new Point(size * 24 / 100, size * 36 / 100);

        graphics.setColor(Color.WHITE);

        arrow = new Polygon();
        for (Point vertex : vertexes) {
            arrow.addPoint((int) vertex.getX(), (int) vertex.getY());
        }
        graphics.fill(arrow);

        graphics.dispose();

        return Toolkit.getDefaultToolkit().createCustomCursor(cursorImage, new Point(size / 2, size / 2), "bigHand");
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (CommunicationMessages.BIG_POINTER_VISIBILITY_CHANGED.equals(evt.getPropertyName())
                || CommunicationMessages.BIG_POINTER_SIZE_CHANGED.equals(evt.getPropertyName())
                || CommunicationMessages.BIG_POINTER_ROTATION_CHANGED.equals(evt.getPropertyName())) {
            updateCursor();
            propertyChangeSupport.firePropertyChange(CommunicationMessages.BIG_POINTER_IMAGE_UPDATE_REQUEST, null,
                    this);
        }
    }

    @Override
    public int getMaxCursorSize() {
        Dimension maxDimension = Toolkit.getDefaultToolkit().getBestCursorSize(1024, 1024);
        int minDimension = maxDimension.width;
        if (maxDimension.height < minDimension) {
            minDimension = maxDimension.height;
        }
        return minDimension;
    }

    @Override
    public Collection<Hint<String>> getHints() {
        Collection<Hint<String>> hints = new ArrayList<>();
        hints.add(new StringHint("Press 5 on the numeric keypad to hide or show a bigger pointer."));
        hints.add(new StringHint(
                "If the bigger pointer is visible, you can change its orientation by pressing the numbers on the numeric keypad. The arrow points from the number you press to the 5 at the center."));
        return hints;
    }
}
