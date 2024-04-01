package com.threeamigos.pixelpeeper.data;

import com.drew.lang.Rational;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Comparator;

/**
 * The value associated to a single {@link ExifTag}. Each tag has its type,
 * so the ExifValue is stored as an Object.<br/>
 * As picture can be taken with the camera rotated in different ways,
 * PICTURE_ORIENTATION_... constants are provided to help the application correctly
 * preprocess the images before showing them. This information is stored in its
 * specific tag, but as this tag is surely one of interest, constants have been
 * added to help identify the preprocessing needed before the image can be
 * correctly visualized.<br/>
 * Otherwise, tags have a human-readable description and a value that can be
 * a String (e.g. camera manufacturer), an integer (e.g. ISO), decimal numbers
 * or {@link com.drew.lang.Rational} values.
 *
 * @author Stefano Reksten
 */
public class ExifValue {

    public static final int PICTURE_ORIENTATION_AS_IS = 1;
    public static final int PICTURE_ORIENTATION_FLIP_HORIZONTALLY = 2;
    public static final int PICTURE_ORIENTATION_CLOCKWISE_180 = 3;
    public static final int PICTURE_ORIENTATION_FLIP_VERTICALLY = 4;
    public static final int PICTURE_ORIENTATION_ANTICLOCKWISE_90_FLIP_VERTICALLY = 5;
    public static final int PICTURE_ORIENTATION_ANTICLOCKWISE_90 = 6;
    public static final int PICTURE_ORIENTATION_CLOCKWISE_90_FLIP_VERTICALLY = 7;
    public static final int PICTURE_ORIENTATION_CLOCKWISE_90 = 8;

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

    private String asComparable() {
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

    /**
     * Transforms the {@link ExifTag} value to a Float where applicable, in order to
     * be able to filter on ranges of values.
     */
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

    /**
     * A Comparator that can be used to sort ExifValues.
     */
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
