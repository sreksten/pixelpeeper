package com.threeamigos.pixelpeeper.instances;

import com.threeamigos.common.util.interfaces.ui.UserInputTracker;
import com.threeamigos.pixelpeeper.implementations.ui.UserInputTrackerImpl;

public class UserInputTrackerInstance {

    private static final UserInputTrackerImpl instance = new UserInputTrackerImpl();

    public static UserInputTrackerImpl get() {
        return instance;
    }

    private UserInputTrackerInstance() {
    }
}
