package com.threeamigos.pixelpeeper.interfaces.preferences.flavours;

import com.threeamigos.common.util.interfaces.preferences.Preferences;

public interface CursorPreferences extends Preferences {

    boolean BIG_POINTER_VISIBLE_DEFAULT = false;
    int BIG_POINTER_MIN_SIZE = 32;
    int BIG_POINTER_SIZE_STEP = 16;
    int BIG_POINTER_SIZE_DEFAULT = 100;
    Rotation POINTER_ROTATION_DEFAULT = Rotation.ROTATION_3;

    default String getDescription() {
        return "Big Pointer preferences";
    }

    void setBigPointerVisible(boolean bigPointerVisible);

    boolean isBigPointerVisible();

    void setBigPointerSize(int bigPointerSize);

    int getBigPointerSize();

    void setBigPointerRotation(Rotation rotation);

    Rotation getBigPointerRotation();

    enum Rotation {

        ROTATION_1((float) (7 * Math.PI / 4)),
        ROTATION_2((float) (6 * Math.PI / 4)),
        ROTATION_3((float) (5 * Math.PI / 4)),
        ROTATION_4(0.0f),
        ROTATION_6((float) (Math.PI)),
        ROTATION_7((float) (Math.PI / 4)),
        ROTATION_8((float) (Math.PI / 2)),
        ROTATION_9((float) (3 * Math.PI / 4));

        private float radians;

        Rotation(float radians) {
            this.radians = radians;
        }

        public float getRadians() {
            return radians;
        }

    }

}
