package com.threeamigos.imageviewer.implementations.ui.plugins;

import java.beans.PropertyChangeEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import com.threeamigos.imageviewer.interfaces.datamodel.CommunicationMessages;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.BigPointerPreferences;
import com.threeamigos.imageviewer.interfaces.ui.CursorManager;

public class BigPointerPlugin extends AbstractMainWindowPlugin {

	private final BigPointerPreferences bigPointerPreferences;
	private final CursorManager cursorManager;

	private Map<Integer, JMenuItem> bigPointerBySize = new HashMap<>();

	public BigPointerPlugin(BigPointerPreferences bigPointerPreferences, CursorManager cursorManager) {
		super();
		this.bigPointerPreferences = bigPointerPreferences;
		this.cursorManager = cursorManager;
	}

	@Override
	public void createMenu() {

		JMenu imageHandlingMenu = mainWindow.getMenu("Image handling");

		addCheckboxMenuItem(imageHandlingMenu, "Show big pointer", SHOW_BIG_POINTER_KEY,
				bigPointerPreferences.isBigPointerVisible(), event -> {
					bigPointerPreferences.setBigPointerVisible(!bigPointerPreferences.isBigPointerVisible());
				});
		JMenu bigPointerSizeMenu = new JMenu("Big pointer size");
		imageHandlingMenu.add(bigPointerSizeMenu);
		int maxDimension = cursorManager.getMaxCursorSize();
		for (int pointerSize = BigPointerPreferences.BIG_POINTER_MIN_SIZE; pointerSize <= maxDimension; pointerSize += BigPointerPreferences.BIG_POINTER_SIZE_STEP) {
			final int currentSize = pointerSize;
			JMenuItem pointerSizeItem = addCheckboxMenuItem(bigPointerSizeMenu, String.valueOf(pointerSize), -1,
					pointerSize - 1 == bigPointerPreferences.getBigPointerSize(), event -> {
						bigPointerPreferences.setBigPointerSize(currentSize - 1);
					});
			bigPointerBySize.put(pointerSize, pointerSizeItem);
		}

	}

	private void updateBigPointerSizeMenu(final int pointerSize) {
		for (Map.Entry<Integer, JMenuItem> entry : bigPointerBySize.entrySet()) {
			entry.getValue().setSelected(entry.getKey() == pointerSize);
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(CommunicationMessages.BIG_POINTER_PREFERENCES_CHANGED)) {
			updateBigPointerSizeMenu(bigPointerPreferences.getBigPointerSize());
		}
	}

}
