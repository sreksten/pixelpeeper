package com.threeamigos.pixelpeeper.interfaces.ui;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.threeamigos.common.util.interfaces.ui.HintsProducer;

public enum KeyRegistry implements HintsProducer {

	SHOW_ABOUT_KEY(KeyEvent.VK_A, "A", "You can show the About window by pressing the 'A' key."),

	BROWSE_DIRECTORY_KEY(KeyEvent.VK_B, "B", "You can open the directory browser by pressing the 'B' key."),

	NORMALIZE_FOR_CROP_FACTOR_KEY(KeyEvent.VK_C, "C", "You can normalize for crop factor by pressing the 'C' button."),

	OPEN_DRAG_AND_DROP_PANEL_KEY(
			KeyEvent.VK_D,
			"D",
			"You can open and close the drag and drop window by pressing the 'D' button."),

	SHOW_EDGES_KEY(KeyEvent.VK_E, "E", "You can show or hide the edges by pressing the 'E' button."),

	SHOW_GRID_KEY(KeyEvent.VK_G, "G", "You can show or hide the grid by pressing the 'G' button."),

	SHOW_HINTS_KEY(KeyEvent.VK_H, "H", "You can show the hints window by pressing the 'H' key."),

	MOVE_ALL_IMAGES_KEY(
			KeyEvent.VK_I,
			"I",
			"Images can be moved all together or one by one. You can toggle this behaviour by pressing the 'I' key."),

	NORMALIZE_FOR_FOCAL_LENGTH_KEY(KeyEvent.VK_L, "L", "You can normalize for focal length by pressing the 'L' key."),

	MOVEMENT_IN_PERCENTAGE_KEY(
			KeyEvent.VK_M,
			"M",
			"Movement can be fixed or relative to the single images' size. You can toggle this behaviour by pressing the 'M' key."),

	SHOW_BIG_POINTER_KEY(
			KeyEvent.VK_NUMPAD5,
			"N",
			"You can show a bigger pointer by pressing the '5' key on your numeric keypad. Its size can be changed by the menu."),

	OPEN_FILES_KEY(KeyEvent.VK_O, "O", "You can open the file browser by pressing the 'O' key."),

	SHOW_EDGES_DETETECTOR_PARAMETERS_KEY(
			KeyEvent.VK_P,
			"P",
			"You can open the parameters window for the edges detector function by pressing the 'P' button."),

	QUIT_KEY(KeyEvent.VK_Q, "Q", "You can quit the program by pressing the 'Q' button."),

	AUTOROTATION_KEY(KeyEvent.VK_R, "R", "You can toggle the automatic image rotation by pressing the 'R' key."),

	SHOW_POSITION_MINIATURE_KEY(
			KeyEvent.VK_S,
			"S",
			"You can show a miniature with the slice's current position by pressing the 'S' key."),

	SHOW_TAGS_KEY(KeyEvent.VK_T, "T", "You can show or hide the tags by pressing the 'T' key."),

	SHOW_TAGS_OVERRIDING_PREFERENCES_KEY(
			KeyEvent.VK_V,
			"V",
			"You can override the single tags' preferences by pressing the 'V' key."),

	MOVEMENT_APPLIED_TO_ALL_IMAGES_TEMPORARILY_INVERTED(
			KeyEvent.VK_CONTROL,
			"Control",
			"You can momentarily invert the image moving behaviour by keeping the 'Control' key pressed."),

	ANNOTATE_KEY(
			KeyEvent.VK_SHIFT,
			"Shift",
			"You can annotate an image by keeping the 'Shift' key pressed. Change color, size and transparency of the brush with the controls panel in the lower part of the window."),

	UNDO_KEY(KeyEvent.VK_U, "U", "You can undo the last annotation by pressing the 'U' key."),

	DELETE_KEY(
			KeyEvent.VK_DELETE,
			"Delete",
			"You can clear all annotations for the last selected slice by pressing the 'Delete' button."),

	ENLARGE_KEY(
			KeyEvent.VK_ADD,
			"Numeric Keypad +",
			"You can zoom in or enlarge the grid by pressing the '+' key on the numeric keypad."),

	REDUCE_KEY(
			KeyEvent.VK_SUBTRACT,
			"Numeric keypad -",
			"You can zoom out or shrink the grid by pressing the '-' key on the numeric keypad."),

	NO_KEY(-1, "No key");

	private int keyCode;
	private String keyName;
	private String hint;

	private KeyRegistry(int keyCode, String keyName) {
		this.keyCode = keyCode;
		this.keyName = keyName;
	}

	private KeyRegistry(int keyCode, String keyName, String hint) {
		this.keyCode = keyCode;
		this.keyName = keyName;
		this.hint = hint;
	}

	public int getKeyCode() {
		return keyCode;
	}

	public String getKeyName() {
		return keyName;
	}

	@Override
	public Collection<String> getHints() {
		List<String> hints = new ArrayList<>();
		for (KeyRegistry current : values()) {
			if (current.hint != null) {
				hints.add(current.hint);
			}
		}
		return hints;
	}
}
