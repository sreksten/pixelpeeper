package com.threeamigos.pixelpeeper.implementations.ui.plugins;

import com.threeamigos.pixelpeeper.implementations.eventbus.EventBus;
import com.threeamigos.pixelpeeper.implementations.eventbus.events.BigPointerSizeChangedEvent;
import com.threeamigos.pixelpeeper.implementations.eventbus.events.BigPointerVisibilityChangedEvent;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.CursorPreferences;
import com.threeamigos.pixelpeeper.interfaces.ui.CursorManager;
import com.threeamigos.pixelpeeper.interfaces.ui.KeyRegistry;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

public class CursorPlugin extends AbstractMainWindowPlugin {

    private final CursorPreferences bigPointerPreferences;
    private final CursorManager cursorManager;

    private JCheckBoxMenuItem bigPointerVisibleMenuItem;
    private final Map<Integer, JMenuItem> bigPointerBySize = new HashMap<>();

    public CursorPlugin(CursorPreferences bigPointerPreferences, CursorManager cursorManager) {
        super();
        this.bigPointerPreferences = bigPointerPreferences;
        this.cursorManager = cursorManager;

        EventBus eventBus = EventBus.get();
        eventBus.subscribe(BigPointerVisibilityChangedEvent.class,
                e -> bigPointerVisibleMenuItem.setSelected(bigPointerPreferences.isBigPointerVisible()));
        eventBus.subscribe(BigPointerSizeChangedEvent.class,
                e -> updateBigPointerSizeMenu(bigPointerPreferences.getBigPointerSize()));
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

    private void updateBigPointerSizeMenu(final int pointerSize) {
        for (Map.Entry<Integer, JMenuItem> entry : bigPointerBySize.entrySet()) {
            entry.getValue().setSelected(entry.getKey() == pointerSize);
        }
    }

}
