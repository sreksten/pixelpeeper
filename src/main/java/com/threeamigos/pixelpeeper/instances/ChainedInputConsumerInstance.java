package com.threeamigos.pixelpeeper.instances;

import com.threeamigos.common.util.implementations.ui.ChainedInputConsumer;

public class ChainedInputConsumerInstance {

    private static final ChainedInputConsumer instance = new ChainedInputConsumer();

    public static ChainedInputConsumer get() {
        return instance;
    }

    private ChainedInputConsumerInstance() {
    }
}
