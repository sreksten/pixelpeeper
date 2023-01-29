package com.threeamigos.imageviewer.data;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.drew.lang.Rational;
import com.threeamigos.imageviewer.implementations.helpers.ExifOrientationHelper;

public class ExifMap {

	private Set<ExifTag> tags = new HashSet<>();
	private Map<ExifTag, String> tagToDescriptions = new EnumMap<>(ExifTag.class);
	private Map<ExifTag, Object> tagToObjects = new EnumMap<>(ExifTag.class);

	// This tag is used more often than others, so we will just avoid getting it
	// from the map
	private int pictureOrientation = ExifOrientationHelper.AS_IS;

	public void setIfAbsent(ExifTag exifTag, String value, Object object) {
		if (value == null || value.trim().isEmpty()) {
			return;
		}
		tags.add(exifTag);
		tagToDescriptions.putIfAbsent(exifTag, value);
		tagToObjects.putIfAbsent(exifTag, object);
	}

	public Collection<ExifTag> getTags() {
		return tags;
	}

	public String getTagDescriptive(ExifTag exifTag) {
		return tagToDescriptions.computeIfAbsent(exifTag, t -> "N/A");
	}

	public Object getTagObject(ExifTag exifTag) {
		return tagToObjects.computeIfAbsent(exifTag, t -> "N/A");
	}

	public Float getAsFloat(ExifTag exifTag) {
		Object object = tagToObjects.get(exifTag);
		if (object == null) {
			return null;
		} else if (object instanceof Integer) {
			return Float.valueOf((Integer) object);
		} else if (object instanceof Float) {
			return (Float) object;
		} else if (object instanceof Double) {
			return Float.valueOf((float) ((Double) object).doubleValue());
		} else if (object instanceof BigInteger) {
			return Float.valueOf((float) ((BigInteger) object).intValue());
		} else if (object instanceof BigDecimal) {
			return Float.valueOf((float) ((BigDecimal) object).doubleValue());
		} else if (object instanceof Rational) {
			Rational rational = (Rational) object;
			return Float.valueOf((float) rational.getNumerator() / (float) rational.getDenominator());
		} else {
			throw new IllegalArgumentException("Don't know how to convert an instance of " + object.getClass());
		}
	}

	public void setPictureOrientation(int pictureOrientation) {
		this.pictureOrientation = pictureOrientation;
	}

	public int getPictureOrientation() {
		return pictureOrientation;
	}

}
