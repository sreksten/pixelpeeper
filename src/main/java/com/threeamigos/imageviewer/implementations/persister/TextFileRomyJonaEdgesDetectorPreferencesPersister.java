package com.threeamigos.imageviewer.implementations.persister;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.StringTokenizer;

import com.threeamigos.common.util.interfaces.ExceptionHandler;
import com.threeamigos.common.util.preferences.filebased.interfaces.RootPathProvider;
import com.threeamigos.imageviewer.interfaces.persister.Persister;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.RomyJonaEdgesDetectorPreferences;

public class TextFileRomyJonaEdgesDetectorPreferencesPersister extends
		TextFilePersister<RomyJonaEdgesDetectorPreferences> implements Persister<RomyJonaEdgesDetectorPreferences> {

	private static final String ROMY_JONA_EDGES_DETECTOR_PREFERENCES_FILENAME = "romy_jona_edge_detector.preferences";

	private static final String PUPPAMENTO = "puppamento";
	private static final String A_NASTRO = "a_nastro";

	public TextFileRomyJonaEdgesDetectorPreferencesPersister(RootPathProvider rootPathProvider,
			ExceptionHandler exceptionHandler) {
		super(rootPathProvider, exceptionHandler);
	}

	@Override
	public String getNamePart() {
		return ROMY_JONA_EDGES_DETECTOR_PREFERENCES_FILENAME;
	}

	@Override
	protected String getEntityDescription() {
		return "Romy Jona edge detector preferences";
	}

	@Override
	protected void loadFromText(BufferedReader reader, RomyJonaEdgesDetectorPreferences romyJonaEdgesDetectorPreferences)
			throws IOException, IllegalArgumentException {
		int puppamento = RomyJonaEdgesDetectorPreferences.PUPPAMENTO_PREFERENCES_DEFAULT;
		boolean aNastro = RomyJonaEdgesDetectorPreferences.A_NASTRO_PREFERENCES_DEFAULT;

		String line;
		while ((line = reader.readLine()) != null) {
			if (!line.trim().isEmpty()) {
				StringTokenizer st = new StringTokenizer(line, "=");
				String key = st.nextToken();
				String value = st.nextToken();
				if (PUPPAMENTO.equalsIgnoreCase(key)) {
					puppamento = Integer.parseInt(value);
				} else if (A_NASTRO.equalsIgnoreCase(key)) {
					aNastro = Boolean.valueOf(value);
				}
			}
		}

		romyJonaEdgesDetectorPreferences.setPuppamento(puppamento);
		romyJonaEdgesDetectorPreferences.setANastro(aNastro);
	}

	@Override
	protected void save(PrintWriter writer, RomyJonaEdgesDetectorPreferences romyJonaEdgesDetectorPreferences)
			throws IOException {
		writer.println(PUPPAMENTO + "=" + romyJonaEdgesDetectorPreferences.getPuppamento());
		writer.println(A_NASTRO + "=" + romyJonaEdgesDetectorPreferences.isANastro());
	}

}
