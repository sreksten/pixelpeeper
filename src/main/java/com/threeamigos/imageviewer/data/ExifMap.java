package com.threeamigos.imageviewer.data;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;

import com.drew.lang.Rational;
import com.threeamigos.imageviewer.implementations.helpers.ExifOrientationHelper;

public class ExifMap {

	private Map<ExifTag, ExifValue> map = new EnumMap<>(ExifTag.class);

	// This tag is used more often than others, so we will just avoid getting it
	// from the map
	private int pictureOrientation = ExifOrientationHelper.AS_IS;

	public void setIfAbsent(ExifTag exifTag, String value, Object object) {
		if (value == null || value.trim().isEmpty()) {
			return;
		}
		map.put(exifTag, new ExifValue(exifTag, value, object));
	}

	public Collection<ExifTag> getKeys() {
		return map.keySet();
	}

	public ExifValue getExifValue(ExifTag exifTag) {
		return map.get(exifTag);
	}

	public String getTagDescriptive(ExifTag exifTag) {
		ExifValue exifValue = map.get(exifTag);
		if (exifValue == null) {
			return "N/A";
		} else {
			return exifValue.getDescription();
		}
	}

	public Float getAsFloat(ExifTag exifTag) {
		ExifValue exifValue = map.get(exifTag);
		if (exifValue == null) {
			return null;
		}
		Object object = exifValue.getValue();
		if (object instanceof Integer) {
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
			throw new IllegalArgumentException(
					"Don't know how to convert to a float an instance of " + object.getClass());
		}
	}

	public void setPictureOrientation(int pictureOrientation) {
		this.pictureOrientation = pictureOrientation;
	}

	public int getPictureOrientation() {
		return pictureOrientation;
	}

	public boolean matches(Map<ExifTag, Collection<ExifValue>> selectionMap) {
		for (Map.Entry<ExifTag, Collection<ExifValue>> selectionEntry : selectionMap.entrySet()) {
			ExifTag selectedTag = selectionEntry.getKey();
			Collection<ExifValue> selectedValues = selectionEntry.getValue();
			ExifValue value = getExifValue(selectedTag);
			if (!selectedValues.contains(value)) {
				return false;
			}
		}
		return true;
	}

}
