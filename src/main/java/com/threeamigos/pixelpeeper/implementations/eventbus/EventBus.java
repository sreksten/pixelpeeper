package com.threeamigos.pixelpeeper.implementations.eventbus;

import javax.swing.SwingUtilities;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Lightweight synchronous event bus for Swing.
 * Publishers call publish(event); subscribers call subscribe(Type.class, handler).
 * Delivery always happens on the Event Dispatch Thread.
 *
 * @author Stefano Reksten
 */
public class EventBus {

    private static final EventBus INSTANCE = new EventBus();

    private final Map<Class<?>, List<Consumer<Object>>> subscribers = new ConcurrentHashMap<>();

    private EventBus() {
    }

    public static EventBus get() {
        return INSTANCE;
    }

    @SuppressWarnings("unchecked")
    public <T> void subscribe(Class<T> eventType, Consumer<T> listener) {
        subscribers
                .computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>())
                .add((Consumer<Object>) listener);
    }

    public <T> void unsubscribe(Class<T> eventType, Consumer<T> listener) {
        List<Consumer<Object>> list = subscribers.get(eventType);
        if (list != null) {
            list.remove(listener);
        }
    }

    public void publish(Object event) {
        List<Consumer<Object>> list = subscribers.get(event.getClass());
        if (list == null || list.isEmpty()) {
            return;
        }
        if (SwingUtilities.isEventDispatchThread()) {
            list.forEach(c -> c.accept(event));
        } else {
            SwingUtilities.invokeLater(() -> list.forEach(c -> c.accept(event)));
        }
    }
}
