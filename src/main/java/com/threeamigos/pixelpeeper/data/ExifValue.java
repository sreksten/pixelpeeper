package com.threeamigos.pixelpeeper.data;

import com.drew.lang.Rational;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Comparator;

public class ExifValue {

    private final ExifTag exifTag;
    private final String description;
    private final Object value;

    ExifValue(ExifTag exifTag, String description, Object value) {
        this.exifTag = exifTag;
        this.description = description;
        this.value = value;
    }

    public ExifTag getExifTag() {
        return exifTag;
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
            return format((float) ((Double) value).doubleValue());
        } else if (value instanceof BigInteger) {
            return format((float) ((BigInteger) value).intValue());
        } else if (value instanceof BigDecimal) {
            return format((float) ((BigDecimal) value).doubleValue());
        } else if (value instanceof Rational) {
            Rational rational = (Rational) value;
            return format((float) rational.getNumerator() / (float) rational.getDenominator());
        } else {
            return value.toString();
        }
    }

    public Float asFloat() {
        Object object = getValue();
        if (object instanceof Integer) {
            return Float.valueOf((Integer) object);
        } else if (object instanceof Float) {
            return (Float) object;
        } else if (object instanceof Double) {
            return (float) ((Double) object).doubleValue();
        } else if (object instanceof BigInteger) {
            return (float) ((BigInteger) object).intValue();
        } else if (object instanceof BigDecimal) {
            return (float) ((BigDecimal) object).doubleValue();
        } else if (object instanceof Rational) {
            Rational rational = (Rational) object;
            return (float) rational.getNumerator() / (float) rational.getDenominator();
        } else {
            throw new IllegalArgumentException(
                    "Don't know how to convert to a float an instance of " + object.getClass());
        }
    }

    private String format(Float floatValue) {
        return String.format("%16.6f", floatValue);
    }

    public static Comparator<ExifValue> getComparator() {
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
