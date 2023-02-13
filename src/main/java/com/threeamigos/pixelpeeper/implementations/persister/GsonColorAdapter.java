package com.threeamigos.pixelpeeper.implementations.persister;

import java.awt.Color;
import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class GsonColorAdapter implements JsonSerializer<Color>, JsonDeserializer<Color> {

	@Override
	public JsonElement serialize(Color src, Type typeOfSrc, JsonSerializationContext context) {
		return new JsonPrimitive(
				String.format("%02X%02X%02X%02X", src.getAlpha(), src.getRed(), src.getGreen(), src.getBlue()));
	}

	@Override
	public Color deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
		String argb = json.getAsJsonPrimitive().getAsString();
		if (argb == null || argb.length() != 8) {
			throw new IllegalArgumentException("Invalid color representation " + argb);
		}
		Integer color = Integer.parseUnsignedInt(argb, 16);
		try {
			return new Color(color);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Invalid color representation " + argb);
		}
	}
}
