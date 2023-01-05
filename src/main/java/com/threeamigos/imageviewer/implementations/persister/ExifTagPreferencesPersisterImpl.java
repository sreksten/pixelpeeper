package com.threeamigos.imageviewer.implementations.persister;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

import com.threeamigos.imageviewer.data.ExifTag;
import com.threeamigos.imageviewer.interfaces.persister.Persister;
import com.threeamigos.imageviewer.interfaces.ui.ExifTagPreferences;

public class ExifTagPreferencesPersisterImpl extends AbstractPreferencesPersisterImpl<ExifTagPreferences>
		implements Persister<ExifTagPreferences> {

	private static final String TAG_PREFERENCES_FILENAME = "tag.preferences";

	@Override
	public String getNamePart() {
		return TAG_PREFERENCES_FILENAME;
	}

	@Override
	protected String getEntityDescription() {
		return "visible tags";
	}

	@Override
	protected void loadImpl(BufferedReader reader, ExifTagPreferences exifTagPreferences) throws IOException {

		Map<ExifTag, Boolean> persistentMap = new EnumMap<>(ExifTag.class);

		String line;
		while ((line = reader.readLine()) != null) {
			if (!line.isEmpty() && !line.isBlank()) {
				StringTokenizer st = new StringTokenizer(line, "=");
				ExifTag tag = ExifTag.valueOf(st.nextToken());
				Boolean value = Boolean.valueOf(st.nextToken());
				persistentMap.put(tag, value);
			}
		}

		for (Entry<ExifTag, Boolean> entry : persistentMap.entrySet()) {
			exifTagPreferences.setTagVisible(entry.getKey(), entry.getValue());
		}
	}

	@Override
	protected void saveImpl(PrintWriter writer, ExifTagPreferences exifTagPreferences) throws IOException {
		for (ExifTag tag : ExifTag.values()) {
			writer.println(tag.name() + '=' + exifTagPreferences.isTagVisible(tag));
		}
	}

}
