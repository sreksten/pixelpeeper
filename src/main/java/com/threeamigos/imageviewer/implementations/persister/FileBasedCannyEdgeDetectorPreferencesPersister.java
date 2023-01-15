package com.threeamigos.imageviewer.implementations.persister;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.StringTokenizer;

import com.threeamigos.common.util.interfaces.ExceptionHandler;
import com.threeamigos.common.util.preferences.filebased.interfaces.PreferencesRootPathProvider;
import com.threeamigos.imageviewer.interfaces.persister.Persister;
import com.threeamigos.imageviewer.interfaces.preferences.CannyEdgeDetectorPreferences;

public class FileBasedCannyEdgeDetectorPreferencesPersister
		extends FileBasedAbstractPreferencesPersister<CannyEdgeDetectorPreferences>
		implements Persister<CannyEdgeDetectorPreferences> {

	private static final String CANNY_EDGE_DETECTOR_PREFERENCES_FILENAME = "canny_edge_detector.preferences";

	private static final String SHOW_EDGE_IMAGES = "show_edge_images";
	private static final String EDGE_IMAGES_TRANSPARENCY = "edge_images_transparency";

	private static final String LOW_THRESHOLD = "low_threshold";
	private static final String HIGH_THRESHOLD = "high_threshold";
	private static final String GAUSSIAN_KERNEL_RADIUS = "gaussian_kernel_radius";
	private static final String GAUSSIAN_KERNEL_WIDTH = "gaussian_kernel_width";
	private static final String CONTRAST_NORMALIZED = "contrast_normalized";

	public FileBasedCannyEdgeDetectorPreferencesPersister(PreferencesRootPathProvider rootPathProvider,
			ExceptionHandler exceptionHandler) {
		super(rootPathProvider, exceptionHandler);
	}

	@Override
	public String getNamePart() {
		return CANNY_EDGE_DETECTOR_PREFERENCES_FILENAME;
	}

	@Override
	protected String getEntityDescription() {
		return "Canny edge detector";
	}

	@Override
	protected void loadImpl(BufferedReader reader, CannyEdgeDetectorPreferences cannyEdgesDetectorPreferences)
			throws IOException, IllegalArgumentException {
		boolean showEdgeImages = CannyEdgeDetectorPreferences.SHOWING_EDGE_IMAGES_DEFAULT;
		int edgeImagesTransparency = CannyEdgeDetectorPreferences.EDGE_IMAGES_TRANSPARENCY_DEFAULT;
		float lowThreshold = CannyEdgeDetectorPreferences.LOW_THRESHOLD_PREFERENCES_DEFAULT;
		float highThreshold = CannyEdgeDetectorPreferences.HIGH_THRESHOLD_PREFERENCES_DEFAULT;
		float gaussianKernelRadius = CannyEdgeDetectorPreferences.GAUSSIAN_KERNEL_RADIUS_DEFAULT;
		int gaussianKernelWidth = CannyEdgeDetectorPreferences.GAUSSIAN_KERNEL_WIDTH_DEFAULT;
		boolean contrastNormalized = CannyEdgeDetectorPreferences.CONTRAST_NORMALIZED_DEFAULT;

		String line;
		while ((line = reader.readLine()) != null) {
			if (!line.trim().isEmpty()) {
				StringTokenizer st = new StringTokenizer(line, "=");
				String key = st.nextToken();
				String value = st.nextToken();
				if (SHOW_EDGE_IMAGES.equals(key)) {
					showEdgeImages = Boolean.valueOf(value);
				} else if (EDGE_IMAGES_TRANSPARENCY.equals(key)) {
					edgeImagesTransparency = Integer.parseInt(value);
				} else if (LOW_THRESHOLD.equalsIgnoreCase(key)) {
					lowThreshold = Float.parseFloat(value);
				} else if (HIGH_THRESHOLD.equalsIgnoreCase(key)) {
					highThreshold = Float.parseFloat(value);
				} else if (GAUSSIAN_KERNEL_RADIUS.equalsIgnoreCase(key)) {
					gaussianKernelRadius = Float.parseFloat(value);
				} else if (GAUSSIAN_KERNEL_WIDTH.equalsIgnoreCase(key)) {
					gaussianKernelWidth = Integer.parseInt(value);
				} else if (CONTRAST_NORMALIZED.equalsIgnoreCase(key)) {
					contrastNormalized = Boolean.valueOf(value);
				}
			}
		}

		checkBoundaries(lowThreshold, highThreshold, gaussianKernelRadius, gaussianKernelWidth);

		cannyEdgesDetectorPreferences.setShowEdgeImages(showEdgeImages);
		cannyEdgesDetectorPreferences.setEdgeImagesTransparency(edgeImagesTransparency);

		cannyEdgesDetectorPreferences.setLowThreshold(lowThreshold);
		cannyEdgesDetectorPreferences.setHighThreshold(highThreshold);
		cannyEdgesDetectorPreferences.setGaussianKernelRadius(gaussianKernelRadius);
		cannyEdgesDetectorPreferences.setGaussianKernelWidth(gaussianKernelWidth);
		cannyEdgesDetectorPreferences.setContrastNormalized(contrastNormalized);
	}

	private void checkBoundaries(float lowThreshold, float highThreshold, float gaussianKernelRadius,
			int gaussianKernelWidth) {
		if (lowThreshold < 0) {
			throw new IllegalArgumentException(
					"Invalid Canny Edges Detector preferences: low threshold must be greater or equal than 0");
		}
		if (highThreshold < 0) {
			throw new IllegalArgumentException(
					"Invalid Canny Edges Detector preferences: high threshold must be greater or equal than 0");
		}
		if (gaussianKernelRadius < 0.1f) {
			throw new IllegalArgumentException(
					"Invalid Canny Edges Detector preferences: gaussian kernel radius must be greater or equal than 0.1");
		}
		if (gaussianKernelWidth < 2) {
			throw new IllegalArgumentException(
					"Invalid Canny Edges Detector preferences: gaussian kernel width must be greater or equal than 2");
		}
	}

	@Override
	protected void saveImpl(PrintWriter writer, CannyEdgeDetectorPreferences cannyEdgesDetectorPreferences)
			throws IOException {
		writer.println(SHOW_EDGE_IMAGES + "=" + cannyEdgesDetectorPreferences.isShowEdgeImages());
		writer.println(EDGE_IMAGES_TRANSPARENCY + "=" + cannyEdgesDetectorPreferences.getEdgeImagesTransparency());

		writer.println(LOW_THRESHOLD + "=" + cannyEdgesDetectorPreferences.getLowThreshold());
		writer.println(HIGH_THRESHOLD + "=" + cannyEdgesDetectorPreferences.getHighThreshold());
		writer.println(GAUSSIAN_KERNEL_RADIUS + "=" + cannyEdgesDetectorPreferences.getGaussianKernelRadius());
		writer.println(GAUSSIAN_KERNEL_WIDTH + "=" + cannyEdgesDetectorPreferences.getGaussianKernelWidth());
		writer.println(CONTRAST_NORMALIZED + "=" + cannyEdgesDetectorPreferences.isContrastNormalized());
	}

}
