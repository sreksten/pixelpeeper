package com.threeamigos.imageviewer.implementations.persister;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.StringTokenizer;

import com.threeamigos.common.util.interfaces.ExceptionHandler;
import com.threeamigos.common.util.preferences.filebased.interfaces.RootPathProvider;
import com.threeamigos.imageviewer.interfaces.edgedetect.EdgesDetectorFlavour;
import com.threeamigos.imageviewer.interfaces.persister.Persister;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.EdgesDetectorPreferences;

public class TextFileEdgesDetectorPreferencesPersister extends TextFilePersister<EdgesDetectorPreferences>
		implements Persister<EdgesDetectorPreferences> {

	private static final String EDGES_DETECTOR_PREFERENCES_FILENAME = "edges_detector.preferences";

	private static final String SHOW_EDGES = "show_edges";
	private static final String EDGES_TRANSPARENCY = "edges_transparency";
	private static final String EDGES_DETECTOR_FLAVOUR = "edges_detector_flavour";

	public TextFileEdgesDetectorPreferencesPersister(RootPathProvider rootPathProvider,
			ExceptionHandler exceptionHandler) {
		super(rootPathProvider, exceptionHandler);
	}

	@Override
	public String getNamePart() {
		return EDGES_DETECTOR_PREFERENCES_FILENAME;
	}

	@Override
	protected String getEntityDescription() {
		return "Edges detector preferences";
	}

	@Override
	protected void loadFromText(BufferedReader reader, EdgesDetectorPreferences edgesDetectorPreferences)
			throws IOException, IllegalArgumentException {
		boolean showEdges = EdgesDetectorPreferences.SHOW_EDGES_DEFAULT;
		int edgesTransparency = EdgesDetectorPreferences.EDGES_TRANSPARENCY_DEFAULT;
		EdgesDetectorFlavour flavour = EdgesDetectorPreferences.EDGES_DETECTOR_FLAVOUR_DEFAULT;

		String line;
		while ((line = reader.readLine()) != null) {
			if (!line.trim().isEmpty()) {
				StringTokenizer st = new StringTokenizer(line, "=");
				String key = st.nextToken();
				String value = st.nextToken();
				if (SHOW_EDGES.equals(key)) {
					showEdges = Boolean.valueOf(value);
				} else if (EDGES_TRANSPARENCY.equals(key)) {
					edgesTransparency = Integer.parseInt(value);
				} else if (EDGES_DETECTOR_FLAVOUR.equals(key)) {
					flavour = EdgesDetectorFlavour.valueOf(value);
				}
			}
		}

		edgesDetectorPreferences.setShowEdges(showEdges);
		edgesDetectorPreferences.setEdgesTransparency(edgesTransparency);
		edgesDetectorPreferences.setEdgesDetectorFlavour(flavour);
	}

	@Override
	protected void save(PrintWriter writer, EdgesDetectorPreferences edgesDetectorPreferences) throws IOException {
		writer.println(SHOW_EDGES + "=" + edgesDetectorPreferences.isShowEdges());
		writer.println(EDGES_TRANSPARENCY + "=" + edgesDetectorPreferences.getEdgesTransparency());
		writer.println(EDGES_DETECTOR_FLAVOUR + "=" + edgesDetectorPreferences.getEdgesDetectorFlavour());
	}

}
