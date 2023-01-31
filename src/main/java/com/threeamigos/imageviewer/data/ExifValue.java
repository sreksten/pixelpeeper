package com.threeamigos.imageviewer.data;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Comparator;

import com.drew.lang.Rational;

public class ExifValue {

	private String description;
	private Object value;

	ExifValue(String description, Object value) {
		this.description = description;
		this.value = value;
	}

	public String getDescription() {
		return description;
	}

	public Object getValue() {
		return value;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ExifValue)) {
			return false;
		}
		ExifValue other = (ExifValue) obj;
		return description.equals(other.description);
	}

	@Override
	public int hashCode() {
		return description.hashCode();
	}

	@Override
	public String toString() {
		return description;
	}

	public String asComparable() {
		if (value instanceof Integer) {
			return format(Float.valueOf((Integer) value));
		} else if (value instanceof Float) {
			return format((Float) value);
		} else if (value instanceof Double) {
			return format(Float.valueOf((float) ((Double) value).doubleValue()));
		} else if (value instanceof BigInteger) {
			return format(Float.valueOf((float) ((BigInteger) value).intValue()));
		} else if (value instanceof BigDecimal) {
			return format(Float.valueOf((float) ((BigDecimal) value).doubleValue()));
		} else if (value instanceof Rational) {
			Rational rational = (Rational) value;
			return format(Float.valueOf((float) rational.getNumerator() / (float) rational.getDenominator()));
		} else {
			return value.toString();
		}
	}

	private String format(Float floatValue) {
		return String.format("%16.6f", floatValue);
	}

	public static final Comparator<ExifValue> getComparator() {
		return (ev1, ev2) -> {
			if (ev1 == null) {
				return -1;
			} else if (ev2 == null) {
				return 1;
			} else {
				return ev1.asComparable().compareTo(ev2.asComparable());
			}
		};
	}
}
