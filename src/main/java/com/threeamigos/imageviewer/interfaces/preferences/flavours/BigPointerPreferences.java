package com.threeamigos.imageviewer.interfaces.preferences.flavours;

import com.threeamigos.imageviewer.interfaces.preferences.Preferences;

public interface BigPointerPreferences extends Preferences {

	public static final boolean BIG_POINTER_VISIBLE_DEFAULT = false;
	public static final int BIG_POINTER_SIZE_DEFAULT = 100;
	public static final float BIG_POINTER_ROTATION_DEFAULT = (float) (5 * Math.PI / 4);
	public static final Rotation POINTER_ROTATION_DEFAULT = Rotation.ROTATION_3;

	default String getDescription() {
		return "Big Pointer preferences";
	}

	public void setBigPointerVisible(boolean bigPointerVisible);

	public boolean isBigPointerVisible();

	public void setBigPointerSize(int bigPointerSize);

	public int getBigPointerSize();

	public void setBigPointerRotation(Rotation rotation);

	public Rotation getBigPointerRotation();

	public enum Rotation {

		ROTATION_1((float) (7 * Math.PI / 4)), ROTATION_2((float) (6 * Math.PI / 4)),
		ROTATION_3((float) (5 * Math.PI / 4)), ROTATION_4(0.0f), ROTATION_6((float) (Math.PI)),
		ROTATION_7((float) (Math.PI / 4)), ROTATION_8((float) (Math.PI / 2)), ROTATION_9((float) (3 * Math.PI / 4));

		private float radians;

		private Rotation(float radians) {
			this.radians = radians;
		}

		public float getRadians() {
			return radians;
		}

	}

}
