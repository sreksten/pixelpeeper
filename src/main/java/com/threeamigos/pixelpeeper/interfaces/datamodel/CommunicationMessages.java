package com.threeamigos.pixelpeeper.interfaces.datamodel;

public class CommunicationMessages {

    public static final String WINDOW_X_POSITION_CHANGED = "WindowXPositionChanged";
    public static final String WINDOW_Y_POSITION_CHANGED = "WindowYPositionChanged";
    public static final String WINDOW_WIDTH_CHANGED = "WindowWidthChanged";
    public static final String WINDOW_HEIGHT_CHANGED = "WindowHeightChanged";
    public static final String WINDOW_VISIBILITY_CHANGED = "WindowVisibilityChanged";

    public static final String BIG_POINTER_VISIBILITY_CHANGED = "BigPointerVisibilityChanged";
    public static final String BIG_POINTER_SIZE_CHANGED = "BigPointerSizeChanged";
    public static final String BIG_POINTER_ROTATION_CHANGED = "BigPointerRotationChanged";
    public static final String BIG_POINTER_IMAGE_UPDATE_REQUEST = "BigPointerImageChanged";

    public static final String CANNY_LOW_THRESHOLD_CHANGED = "CannyLowThresholdChanged";
    public static final String CANNY_HIGH_THRESHOLD_CHANGED = "CannyHighThresholdChanged";
    public static final String CANNY_GAUSSIAN_KERNEL_RADIUS_CHANGED = "CannyGaussianKernelRadiusChanged";
    public static final String CANNY_GAUSSIAN_KERNEL_WIDTH_CHANGED = "CannyGaussianKernelWidthChanged";
    public static final String CANNY_CONTRAST_NORMALIZED_CHANGED = "CannyContrastNormalizedChanged";

    public static final String PALETTE_FILTER_COLOR_CLASH_CHANGED = "PaletteFilterColorClashChanged";
    public static final String PALETTE_FILTER_SATURATION_THRESHOLD_CHANGED = "PaletteFilterSaturationThresholdChanged";
    public static final String PALETTE_FILTER_LIGHTNESS_MIN_THRESHOLD_CHANGED = "PaletteFilterLightnessMinThresholdChanged";
    public static final String PALETTE_FILTER_LIGHTNESS_MAX_THRESHOLD_CHANGED = "PaletteFilterLightnessMaxThresholdChanged";
    public static final String PALETTE_FILTER_HUE_WEIGHT_CHANGED = "PaletteFilterHueWeightChanged";
    public static final String PALETTE_FILTER_SATURATION_WEIGHT_CHANGED = "PaletteFilterSaturationWeightChanged";
    public static final String PALETTE_FILTER_LIGHTNESS_WEIGHT_CHANGED = "PaletteFilterLightnessWeightChanged";
    public static final String PALETTE_FILTER_SKIN_TONES_MAPPING_CHANGED = "PaletteFilterMapSkinTonesChanged";

    public static final String FILTER_VISIBILITY_CHANGED = "FilterVisibilityChanged";
    public static final String FILTER_TRANSPARENCY_CHANGED = "FilterTransparencyChanged";
    public static final String FILTER_FLAVOR_CHANGED = "FilterFlavorChanged";

    public static final String TAG_VISIBILITY_CHANGED = "TagVisibilityChanged";
    public static final String TAGS_VISIBILITY_CHANGED = "TagsVisibilityChanged";
    public static final String TAGS_VISIBILITY_OVERRIDE_CHANGED = "TagsVisibilityOverrideChanged";
    public static final String TAGS_RENDERING_CHANGED = "TagsRenderingChanged";

    public static final String HINTS_VISIBILITY_AT_STARTUP_CHANGED = "HintsVisibilityAtStartupChanged";
    public static final String HINTS_INDEX_CHANGED = "HintsIndexChanged";

    public static final String AUTOROTATION_CHANGED = "AutoRotationChanged";
    public static final String DISPOSITION_CHANGED = "DispositionChanged";
    public static final String ZOOM_LEVEL_CHANGED = "ZoomLevelChanged";
    public static final String NORMALIZED_FOR_CROP_CHANGED = "NormalizeForCropChanged";
    public static final String NORMALIZE_FOR_FOCAL_LENGTH_CHANGED = "NormalizeForFocalLengthChanged";
    public static final String RELATIVE_MOVEMENT_CHANGED = "RelativeMovementChanged";
    public static final String MOVEMENT_APPLIED_TO_ALL_IMAGES_CHANGED = "MovementAppliedToAllImagesChanged";
    public static final String POSITION_MINIATURE_VISIBILITY_CHANGED = "PositionMiniatureVisibilityChanged";
    public static final String IMAGE_READER_FLAVOR_CHANGED = "ImageReaderFlavorChanged";
    public static final String EXIF_READER_FLAVOR_CHANGED = "ExifReaderFlavorChanged";

    public static final String LAST_PATH_CHANGED = "LastPathChanged";
    public static final String LAST_FILES_CHANGED = "LastFilesChanged";
    public static final String TAG_TO_GROUP_BY_CHANGED = "TagToGroupByChanged";
    public static final String TAG_TO_ORDER_BY_CHANGED = "TagToOrderByChanged";
    public static final String GROUP_INDEX_CHANGED = "GroupIndexChanged";

    public static final String REQUEST_FILTER_CALCULATION = "RequestFilterCalculation";
    public static final String FILTER_CALCULATION_STARTED = "FilterCalculationStarted";
    public static final String FILTER_CALCULATION_COMPLETED = "FilterCalculationCompleted";
    public static final String GRID_VISIBILITY_CHANGED = "GridVisibilityChange";
    public static final String GRID_SPACING_CHANGED = "GridSizeChanged";
    public static final String DATA_MODEL_CHANGED = "DataModelChanged";
    public static final String REQUEST_REPAINT = "RequestRepaint";
    public static final String MOUSE_PRESSED = "MousePressed";
    public static final String MOUSE_RELEASED = "MouseReleased";
    public static final String MOUSE_DRAGGED = "MouseDragged";

    private CommunicationMessages() {
    }
}
