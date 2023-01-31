package com.threeamigos.imageviewer.interfaces.datamodel;

public interface CommunicationMessages {

	public static final String EDGES_VISIBILITY = "EdgesVisibility";

	public static final String EDGES_CALCULATION_STARTED = "EdgesCalculationStarted";

	public static final String EDGES_CALCULATION_COMPLETED = "EdgesCalculationCompleted";

	public static final String BIG_POINTER_PREFERENCES_CHANGED = "BigPointerPreferencesUpdated";

	public static final String BIG_POINTER_IMAGE_CHANGED = "BigPointerChanged";

	public static final String MINIATURE_VISIBILITY_CHANGE = "MiniatureVisibilityChange";

	public static final String GRID_VISIBILITY_CHANGE = "GridVisibilityChange";

	public static final String GRID_SIZE_CHANGED = "GridSizeChanged";

	public static final String ZOOM_LEVEL_CHANGED = "ZoomLevelChanged";

	public static final String DATA_MODEL_CHANGED = "DataModelChanged";

	public static final String REQUEST_REPAINT = "RequestRepaint";

	public static final String MOUSE_PRESSED = "MousePressed";

	public static final String MOUSE_RELEASED = "MouseReleased";

	public static final String MOUSE_DRAGGED = "MouseDragged";
}
