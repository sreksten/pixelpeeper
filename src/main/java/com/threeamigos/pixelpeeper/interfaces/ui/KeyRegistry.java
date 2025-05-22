package com.threeamigos.pixelpeeper.interfaces.ui;

import com.threeamigos.common.util.implementations.ui.StringHint;
import com.threeamigos.common.util.interfaces.ui.Hint;
import com.threeamigos.common.util.interfaces.ui.HintsProducer;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * An enumeration of the various keyboard commands that the end-user can take advantage of when using the
 * application. As there can be a lot of them, sparse within various components, an enumeration is provided
 * to keep track of them in a single place, to (try to) avoid having two actions mapped to the same key.<br/>
 * This enumeration implements the {@link HintsProducer} interface in order to collect the hints related to each
 * key to show them at startup.
 *
 * @author Stefano Reksten
 */
public enum KeyRegistry implements HintsProducer<String> {

    SHOW_ABOUT_KEY(KeyEvent.VK_A, "A", "Opens the About window",
            "You can show the About window by pressing the 'A' key."),

    BROWSE_DIRECTORY_KEY(KeyEvent.VK_B, "B", "Opens the directory browser window",
            "You can open the directory browser by pressing the 'B' key."),

    NORMALIZE_FOR_CROP_FACTOR_KEY(KeyEvent.VK_C, "C", "Applies Crop-factor normalization",
            "You can normalize for crop factor by pressing the 'C' key."),

    OPEN_DRAG_AND_DROP_PANEL_KEY(KeyEvent.VK_D, "D", "Opens or closes the Drag-and-drop window",
            "You can open and close the drag and drop window by pressing the 'D' bey."),

    SHOW_RESULTS_KEY(KeyEvent.VK_F, "F", "Superimposes or hides the current filter results on the image",
            "You can show or hide the filter results by pressing the 'F' key."),

    SHOW_GRID_KEY(KeyEvent.VK_G, "G", "Shows or hides the grid",
            "You can show or hide the grid by pressing the 'G' key."),

    SHOW_HINTS_KEY(KeyEvent.VK_H, "H", "Show hints",
            "You can show the hints window by pressing the 'H' key."),

    MOVE_ALL_IMAGES_KEY(KeyEvent.VK_I, "I", "Switches images moving behaviour between 'together' and 'one by one'",
            "Images can be moved all together or one by one. You can toggle this behaviour by pressing the 'I' key."),

    NORMALIZE_FOR_FOCAL_LENGTH_KEY(KeyEvent.VK_L, "L", "Applies Focal-length normalization",
            "You can normalize for focal length by pressing the 'L' key."),

    MOVEMENT_IN_PERCENTAGE_KEY(KeyEvent.VK_M, "M", "Switches between fixed and relative movement",
            "Movement can be fixed or relative to the single images' size. You can toggle this behaviour by pressing the 'M' key."),

    OPEN_FILES_KEY(KeyEvent.VK_O, "O", "Opens the file browser",
            "You can open the file browser by pressing the 'O' key."),

    SHOW_FILTER_PARAMETERS_KEY(KeyEvent.VK_P, "P", "Opens the Filter parameters window",
            "You can open the parameters window for the filter function by pressing the 'P' key."),

    QUIT_KEY(KeyEvent.VK_Q, "Q", "Quits the program",
            "You can quit the program by pressing the 'Q' key."),

    AUTOROTATION_KEY(KeyEvent.VK_R, "R", "Toggles automatic image rotation",
            "You can toggle the automatic image rotation by pressing the 'R' key."),

    SHOW_POSITION_MINIATURE_KEY(KeyEvent.VK_S, "S", "Shows position miniature",
            "You can show a miniature with the slice's current position by pressing the 'S' key."),

    SHOW_TAGS_KEY(KeyEvent.VK_T, "T", "Show the EXIF tags associated to the current image",
            "You can show or hide the EXIF tags associated to the current image by pressing the 'T' key."),

    UNDO_KEY(KeyEvent.VK_U, "U", "Undoes the last doodle",
            "You can undo the last doodle by pressing the 'U' key."),

    SHOW_TAGS_OVERRIDING_PREFERENCES_KEY(KeyEvent.VK_V, "V", "Shows all tags overriding preferences",
            "You can override the single tags' preferences by pressing the 'V' key."),

    MOVEMENT_APPLIED_TO_ALL_IMAGES_TEMPORARILY_INVERTED(KeyEvent.VK_CONTROL, "Control",
            "Invert movement behaviour temporarily",
            "You can momentarily invert the image moving behaviour by keeping the 'Control' key pressed."),

    ANNOTATE_KEY(KeyEvent.VK_SHIFT, "Shift", "Starts doodling when clicking the mouse",
            "You can annotate an image by keeping the 'Shift' key pressed and then moving the mouse. Change color, size and transparency of the brush with the controls panel in the lower part of the window."),

    DELETE_KEY(KeyEvent.VK_DELETE, "Delete", "Deletes all doodles",
            "You can clear all annotations for the last selected slice by pressing the 'Delete' key."),

    SHOW_BIG_POINTER_KEY(KeyEvent.VK_NUMPAD5, "Numpad 5", "Toggles the big pointer",
            "You can show a bigger pointer by pressing the '5' key on your numeric keypad. Its size can be changed by the menu."),

    CHANGE_BIG_POINTER_ORIENTATION(-1, "Numpad 1 to 9", "Changes the big pointer orientation",
            "You can alter the Big Pointer orientation by clicking keys '1' to '9' (excluding '5') on your numeric keypad."),

    ENLARGE_KEY(KeyEvent.VK_ADD, "Numpad +", "Enlarges the grid",
            "You can zoom in or enlarge the grid by pressing the '+' key on the numeric keypad."),

    REDUCE_KEY(KeyEvent.VK_SUBTRACT, "Numpad -", "Shrinks the grid",
            "You can zoom out or shrink the grid by pressing the '-' key on the numeric keypad."),

    NO_KEY(-1, "No key", "No action");

    private final int keyCode;
    private final String keyName;
    private final String action;
    private final String hint;

    KeyRegistry(int keyCode, String keyName, String action) {
        this.keyCode = keyCode;
        this.keyName = keyName;
        this.action = action;
        this.hint = null;
    }

    KeyRegistry(int keyCode, String keyName, String action, String hint) {
        this.keyCode = keyCode;
        this.keyName = keyName;
        this.action = action;
        this.hint = hint;
    }

    public int getKeyCode() {
        return keyCode;
    }

    public String getKeyName() {
        return keyName;
    }

    public String getAction() {
        return action;
    }

    @Override
    public Collection<Hint<String>> getHints() {
        List<Hint<String>> hints = new ArrayList<>();
        for (KeyRegistry current : values()) {
            if (current.hint != null) {
                hints.add(new StringHint(current.hint));
            }
        }
        return hints;
    }
}
