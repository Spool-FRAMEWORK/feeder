package software.spool.publisher.internal.port;

import software.spool.model.SpoolEvent;

import java.util.function.Consumer;

public interface EventBus {
    void emit(SpoolEvent event, String channel);
    <T extends SpoolEvent> void subscribe(Class<T> eventClass, Consumer<T> handler);
}
