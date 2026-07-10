package com.threeamigos.pixelpeeper.implementations.eventbus.events;

import java.awt.event.MouseEvent;

public class MousePressedEvent {
    public final MouseEvent mouseEvent;

    public MousePressedEvent(MouseEvent mouseEvent) {
        this.mouseEvent = mouseEvent;
    }
}
