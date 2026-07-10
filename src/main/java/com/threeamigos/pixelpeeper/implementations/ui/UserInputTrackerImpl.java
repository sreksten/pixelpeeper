package com.threeamigos.pixelpeeper.implementations.ui;

import com.threeamigos.common.util.interfaces.ui.InputConsumer;
import com.threeamigos.common.util.interfaces.ui.UserInputTracker;
import com.threeamigos.pixelpeeper.implementations.eventbus.EventBus;
import com.threeamigos.pixelpeeper.implementations.eventbus.events.MouseDraggedEvent;
import com.threeamigos.pixelpeeper.implementations.eventbus.events.MousePressedEvent;
import com.threeamigos.pixelpeeper.implementations.eventbus.events.MouseReleasedEvent;
import jakarta.annotation.Nonnull;

import java.awt.event.MouseEvent;

public class UserInputTrackerImpl {

    private MouseEvent oldEvent;

    public @Nonnull InputConsumer getInputConsumer() {
        return new InputAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    EventBus.get().publish(new MousePressedEvent(e));
                    oldEvent = e;
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    EventBus.get().publish(new MouseReleasedEvent());
                    oldEvent = null;
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (oldEvent == null) {
                    oldEvent = e;
                }
                EventBus.get().publish(new MouseDraggedEvent(oldEvent, e));
                oldEvent = e;
            }
        };
    }
}
