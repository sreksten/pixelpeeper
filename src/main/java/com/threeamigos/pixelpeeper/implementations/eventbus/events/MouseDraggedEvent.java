package com.threeamigos.pixelpeeper.implementations.eventbus.events;

import java.awt.event.MouseEvent;

public class MouseDraggedEvent {
    public final MouseEvent oldEvent;
    public final MouseEvent newEvent;

    public MouseDraggedEvent(MouseEvent oldEvent, MouseEvent newEvent) {
        this.oldEvent = oldEvent;
        this.newEvent = newEvent;
    }
}
