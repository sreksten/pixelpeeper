package com.threeamigos.imageviewer.interfaces.ui;

import java.awt.event.KeyEvent;

public interface KeyRegistry {

	public static final int SHOW_ABOUT_KEY = KeyEvent.VK_A;
	public static final int BROWSE_DIRECTORY_KEY = KeyEvent.VK_B;
	public static final int NORMALIZE_FOR_CROP_FACTOR_KEY = KeyEvent.VK_C;
	public static final int OPEN_DRAG_AND_DROP_PANEL_KEY = KeyEvent.VK_D;
	public static final int SHOW_EDGES_KEY = KeyEvent.VK_E;
	public static final int SHOW_GRID_KEY = KeyEvent.VK_G;
	public static final int SHOW_HINTS_KEY = KeyEvent.VK_H;
	public static final int MOVE_ALL_IMAGES_KEY = KeyEvent.VK_I;
	public static final int NORMALIZE_FOR_FOCAL_LENGTH_KEY = KeyEvent.VK_L;
	public static final int MOVEMENT_IN_PERCENTAGE_KEY = KeyEvent.VK_M;
	public static final int SHOW_BIG_POINTER_KEY = KeyEvent.VK_N;
	public static final int OPEN_FILES_KEY = KeyEvent.VK_O;
	public static final int SHOW_EDGES_DETETECTOR_PARAMETERS_KEY = KeyEvent.VK_P;
	public static final int QUIT_KEY = KeyEvent.VK_Q;
	public static final int AUTOROTATION_KEY = KeyEvent.VK_R;
	public static final int SHOW_POSITION_MINIATURE_KEY = KeyEvent.VK_S;
	public static final int SHOW_TAGS_KEY = KeyEvent.VK_T;
	public static final int SHOW_TAGS_OVERRIDING_PREFERENCES_KEY = KeyEvent.VK_V;

	public static final int MOVEMENT_APPLIED_TO_ALL_IMAGES_TEMPORARILY_INVERTED = KeyEvent.VK_CONTROL;
	public static final int DRAWING_KEY = KeyEvent.VK_SHIFT;
	public static final int UNDO_KEY = KeyEvent.VK_U;
	public static final int DELETE_KEY = KeyEvent.VK_DELETE;

	public static final int ENLARGE_KEY = KeyEvent.VK_ADD;
	public static final int REDUCE_KEY = KeyEvent.VK_SUBTRACT;

	public static final int NO_KEY = -1;
}
