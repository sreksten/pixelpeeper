package com.threeamigos.pixelpeeper.instances;

import com.threeamigos.common.util.implementations.messagehandler.CompositeMessageHandler;
import com.threeamigos.common.util.implementations.messagehandler.ConsoleMessageHandler;
import com.threeamigos.common.util.implementations.messagehandler.SwingMessageHandler;
import com.threeamigos.common.util.interfaces.messagehandler.MessageHandler;

public class MessageHandlerInstance {

    private static final MessageHandler instance =
            new CompositeMessageHandler(new SwingMessageHandler(), new ConsoleMessageHandler());

    public static MessageHandler get() {
        return instance;
    }

    private MessageHandlerInstance() {
    }
}
