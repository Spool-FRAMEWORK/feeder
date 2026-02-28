package software.spool.publisher.internal.util;

import software.spool.model.SpoolEvent;
import software.spool.publisher.internal.port.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class InMemoryEventBus implements EventBus {
    private final Map<Class<? extends SpoolEvent>, List<Consumer<SpoolEvent>>> consumers;

    public InMemoryEventBus() {
        consumers = new HashMap<>();
    }

    @Override
    public void emit(SpoolEvent event, String channel) {
        consumers.getOrDefault(event.getClass(), List.of()).forEach(consumer -> consumer.accept(event));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends SpoolEvent> void subscribe(Class<T> eventClass, Consumer<T> handler) {
        consumers.computeIfAbsent(eventClass, k -> new ArrayList<>())
                .add(e -> handler.accept((T) e));
    }
}
