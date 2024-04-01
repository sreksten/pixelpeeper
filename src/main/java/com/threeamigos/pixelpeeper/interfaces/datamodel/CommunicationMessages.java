package com.threeamigos.pixelpeeper.interfaces.datamodel;

//TODO move the messages to the proper classes.
public interface CommunicationMessages {

    String WINDOW_X_POSITION_CHANGED = "WindowXPositionChanged";
    String WINDOW_Y_POSITION_CHANGED = "WindowYPositionChanged";
    String WINDOW_WIDTH_CHANGED = "WindowWidthChanged";
    String WINDOW_HEIGHT_CHANGED = "WindowHeightChanged";
    String WINDOW_VISIBILITY_CHANGED = "WindowVisibilityChanged";

    String BIG_POINTER_VISIBILITY_CHANGED = "BigPointerVisibilityChanged";
    String BIG_POINTER_SIZE_CHANGED = "BigPointerSizeChanged";
    String BIG_POINTER_ROTATION_CHANGED = "BigPointerRotationChanged";
    String BIG_POINTER_IMAGE_UPDATE_REQUEST = "BigPointerImageChanged";

    String CANNY_LOW_THRESHOLD_CHANGED = "CannyLowThresholdChanged";
    String CANNY_HIGH_THRESHOLD_CHANGED = "CannyHighThresholdChanged";
    String CANNY_GAUSSIAN_KERNEL_RADIUS_CHANGED = "CannyGaussianKernelRadiusChanged";
    String CANNY_GAUSSIAN_KERNEL_WIDTH_CHANGED = "CannyGaussianKernelWidthChanged";
    String CANNY_CONTRAST_NORMALIZED_CHANGED = "CannyContrastNormalizedChanged";

    String EDGES_VISIBILITY_CHANGED = "EdgesVisibilityChanged";
    String EDGES_TRANSPARENCY_CHANGED = "EdgesTransparencyChanged";
    String EDGES_DETECTOR_FLAVOUR_CHANGED = "EdgesDetectorFlavourChanged";

    String TAG_VISIBILITY_CHANGED = "TagVisibilityChanged";
    String TAGS_VISIBILITY_CHANGED = "TagsVisibilityChanged";
    String TAGS_VISIBILITY_OVERRIDE_CHANGED = "TagsVisibilityOverrideChanged";
    String TAGS_RENDERING_CHANGED = "TagsRenderingChanged";

    String HINTS_VISIBILITY_AT_STARTUP_CHANGED = "HintsVisibilityAtStartupChanged";
    String HINTS_INDEX_CHANGED = "HintsIndexChanged";

    String AUTOROTATION_CHANGED = "AutoRotationChanged";
    String DISPOSITION_CHANGED = "DispositionChanged";
    String ZOOM_LEVEL_CHANGED = "ZoomLevelChanged";
    String NORMALIZED_FOR_CROP_CHANGED = "NormalizeForCropChanged";
    String NORMALIZE_FOR_FOCAL_LENGTH_CHANGED = "NormalizeForFocalLengthChanged";
    String RELATIVE_MOVEMENT_CHANGED = "RelativeMovementChanged";
    String MOVEMENT_APPLIED_TO_ALL_IMAGES_CHANGED = "MovementAppliedToAllImagesChanged";
    String POSITION_MINIATURE_VISIBILITY_CHANGED = "PositionMiniatureVisibilityChanged";
    String IMAGE_READER_FLAVOUR_CHANGED = "ImageReaderFlavourChanged";
    String EXIF_READER_FLAVOUR_CHANGED = "ExifReaderFlavourChanged";

    String LAST_PATH_CHANGED = "LastPathChanged";
    String LAST_FILES_CHANGED = "LastFilesChanged";
    String TAG_TO_GROUP_BY_CHANGED = "TagToGroupByChanged";
    String TAG_TO_ORDER_BY_CHANGED = "TagToOrderByChanged";
    String GROUP_INDEX_CHANGED = "GroupIndexChanged";

    String REQUEST_EDGES_CALCULATION = "RequestEdgesCalculation";
    String EDGES_CALCULATION_STARTED = "EdgesCalculationStarted";
    String EDGES_CALCULATION_COMPLETED = "EdgesCalculationCompleted";
    String GRID_VISIBILITY_CHANGED = "GridVisibilityChange";
    String GRID_SPACING_CHANGED = "GridSizeChanged";
    String DATA_MODEL_CHANGED = "DataModelChanged";
    String REQUEST_REPAINT = "RequestRepaint";
    String MOUSE_PRESSED = "MousePressed";
    String MOUSE_RELEASED = "MouseReleased";
    String MOUSE_DRAGGED = "MouseDragged";

}
