package com.threeamigos.imageviewer.implementations.persister;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

import com.threeamigos.imageviewer.data.ExifTag;
import com.threeamigos.imageviewer.data.ExifTagVisibility;
import com.threeamigos.imageviewer.interfaces.persister.Persister;
import com.threeamigos.imageviewer.interfaces.preferences.ExifTagPreferences;

public class ExifTagPreferencesPersisterImpl extends AbstractPreferencesPersisterImpl<ExifTagPreferences>
		implements Persister<ExifTagPreferences> {

	private static final String TAG_PREFERENCES_FILENAME = "tag.preferences";

	private static final String TAGS_VISIBLE = "tags_visible";
	private static final String TAG_PREFIX = "TAG_";

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

		boolean tagsVisible = ExifTagPreferences.TAGS_VISIBLE_DEFAULT;
		Map<ExifTag, ExifTagVisibility> persistentMap = new EnumMap<>(ExifTag.class);

		String line;
		while ((line = reader.readLine()) != null) {
			if (!line.trim().isEmpty()) {
				StringTokenizer st = new StringTokenizer(line, "=");
				String key = st.nextToken();
				String value = st.nextToken();
				if (TAGS_VISIBLE.equalsIgnoreCase(key)) {
					tagsVisible = Boolean.valueOf(value);
				} else if (key.toUpperCase().startsWith(TAG_PREFIX)) {
					String tagName = key.substring(TAG_PREFIX.length());
					ExifTag tag = ExifTag.valueOf(tagName);
					ExifTagVisibility visibility = ExifTagVisibility.valueOf(value);
					persistentMap.put(tag, visibility);
				}
			}
		}

		exifTagPreferences.setTagsVisible(tagsVisible);
		for (Entry<ExifTag, ExifTagVisibility> entry : persistentMap.entrySet()) {
			exifTagPreferences.setTagVisibility(entry.getKey(), entry.getValue());
		}
	}

	@Override
	protected void saveImpl(PrintWriter writer, ExifTagPreferences exifTagPreferences) throws IOException {
		writer.println(TAGS_VISIBLE + "=" + exifTagPreferences.isTagsVisible());
		for (ExifTag tag : ExifTag.values()) {
			writer.println(TAG_PREFIX + tag.name() + '=' + exifTagPreferences.getTagVisibility(tag).name());
		}
	}

}
