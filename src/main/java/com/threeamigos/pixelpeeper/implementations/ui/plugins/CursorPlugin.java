package com.threeamigos.pixelpeeper.implementations.ui.plugins;

import com.threeamigos.pixelpeeper.interfaces.datamodel.CommunicationMessages;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavours.CursorPreferences;
import com.threeamigos.pixelpeeper.interfaces.ui.CursorManager;
import com.threeamigos.pixelpeeper.interfaces.ui.KeyRegistry;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;

public class CursorPlugin extends AbstractMainWindowPlugin implements PropertyChangeListener {

    private final CursorPreferences bigPointerPreferences;
    private final CursorManager cursorManager;

    private JCheckBoxMenuItem bigPointerVisibleMenuItem;
    private final Map<Integer, JMenuItem> bigPointerBySize = new HashMap<>();

    public CursorPlugin(CursorPreferences bigPointerPreferences, CursorManager cursorManager) {
        super();
        this.bigPointerPreferences = bigPointerPreferences;
        this.cursorManager = cursorManager;
    }

    @Override
    public void createMenu() {

        JMenu imageHandlingMenu = mainWindow.getMenu("Image handling");

        bigPointerVisibleMenuItem = addCheckboxMenuItem(imageHandlingMenu, "Big pointer", KeyRegistry.NO_KEY,
                bigPointerPreferences.isBigPointerVisible(), event ->
                        bigPointerPreferences.setBigPointerVisible(!bigPointerPreferences.isBigPointerVisible()));
        JMenu bigPointerSizeMenu = new JMenu("Big pointer size");
        imageHandlingMenu.add(bigPointerSizeMenu);
        int maxDimension = cursorManager.getMaxCursorSize();
        for (int pointerSize = CursorPreferences.BIG_POINTER_MIN_SIZE; pointerSize <= maxDimension; pointerSize += CursorPreferences.BIG_POINTER_SIZE_STEP) {
            final int currentSize = pointerSize;
            JMenuItem pointerSizeItem = addCheckboxMenuItem(bigPointerSizeMenu, String.valueOf(pointerSize),
                    KeyRegistry.NO_KEY, pointerSize == bigPointerPreferences.getBigPointerSize(),
                    event -> bigPointerPreferences.setBigPointerSize(currentSize));
            bigPointerBySize.put(pointerSize, pointerSizeItem);
        }

    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(CommunicationMessages.BIG_POINTER_VISIBILITY_CHANGED)) {
            bigPointerVisibleMenuItem.setSelected(bigPointerPreferences.isBigPointerVisible());
        } else if (evt.getPropertyName().equals(CommunicationMessages.BIG_POINTER_SIZE_CHANGED)) {
            updateBigPointerSizeMenu(bigPointerPreferences.getBigPointerSize());
        } else if (evt.getPropertyName().equals(CommunicationMessages.BIG_POINTER_ROTATION_CHANGED)) {
            // Do nothing here
        }
    }

    private void updateBigPointerSizeMenu(final int pointerSize) {
        for (Map.Entry<Integer, JMenuItem> entry : bigPointerBySize.entrySet()) {
            entry.getValue().setSelected(entry.getKey() == pointerSize);
        }
    }

}
