package com.threeamigos.imageviewer.implementations.persister;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.StringTokenizer;

import com.threeamigos.common.util.interfaces.ExceptionHandler;
import com.threeamigos.common.util.preferences.filebased.interfaces.RootPathProvider;
import com.threeamigos.imageviewer.interfaces.persister.Persister;
import com.threeamigos.imageviewer.interfaces.preferences.flavours.BigPointerPreferences;

public class TextFileBigPointerPreferencesPersister extends TextFilePersister<BigPointerPreferences>
		implements Persister<BigPointerPreferences> {

	private static final String BIG_POINTER_PREFERENCES_FILENAME = "big_pointer.preferences";

	private static final String BIG_POINTER_VISIBLE = "big_pointer_visible";
	private static final String BIG_POINTER_SIZE = "big_pointer_size";
	private static final String BIG_POINTER_ROTATION = "big_pointer_rotation";

	public TextFileBigPointerPreferencesPersister(RootPathProvider rootPathProvider,
			ExceptionHandler exceptionHandler) {
		super(rootPathProvider, exceptionHandler);
	}

	@Override
	public String getNamePart() {
		return BIG_POINTER_PREFERENCES_FILENAME;
	}

	@Override
	protected String getEntityDescription() {
		return "big pointer preferences";
	}

	@Override
	protected void loadFromText(BufferedReader reader, BigPointerPreferences pointerPreferences)
			throws IOException, IllegalArgumentException {
		boolean bigPointerVisible = BigPointerPreferences.BIG_POINTER_VISIBLE_DEFAULT;
		int bigPointerSize = BigPointerPreferences.BIG_POINTER_SIZE_DEFAULT;
		float bigPointerRotation = BigPointerPreferences.BIG_POINTER_ROTATION_DEFAULT;

		String line;
		while ((line = reader.readLine()) != null) {
			if (!line.trim().isEmpty()) {
				StringTokenizer st = new StringTokenizer(line, "=");
				String key = st.nextToken();
				String value = st.nextToken();
				if (BIG_POINTER_VISIBLE.equalsIgnoreCase(key)) {
					bigPointerVisible = Boolean.valueOf(value);
				} else if (BIG_POINTER_SIZE.equalsIgnoreCase(key)) {
					bigPointerSize = Integer.parseInt(value);
				} else if (BIG_POINTER_ROTATION.equalsIgnoreCase(key)) {
					bigPointerRotation = Float.parseFloat(value);
				}
			}
		}

		pointerPreferences.setBigPointerVisible(bigPointerVisible);
		pointerPreferences.setBigPointerSize(bigPointerSize);
		pointerPreferences.setBigPointerRotation(bigPointerRotation);
	}

	@Override
	protected void save(PrintWriter writer, BigPointerPreferences pointerPreferences) throws IOException {
		writer.println(BIG_POINTER_VISIBLE + "=" + pointerPreferences.isBigPointerVisible());
		writer.println(BIG_POINTER_SIZE + "=" + pointerPreferences.getBigPointerSize());
		writer.println(BIG_POINTER_ROTATION + "=" + pointerPreferences.getBigPointerRotation());
	}

}
