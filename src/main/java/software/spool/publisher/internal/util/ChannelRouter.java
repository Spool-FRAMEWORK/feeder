package software.spool.publisher.internal.util;

import software.spool.model.SpoolEvent;

import java.util.HashMap;
import java.util.Map;

public class ChannelRouter {
    private final Map<Class<? extends SpoolEvent>, String> routes;

    public ChannelRouter() {
        routes = new HashMap<>();
    }

    public <T extends SpoolEvent> ChannelRouter route(Class<T> eventType, String channel) {
        routes.put(eventType, channel);
        return this;
    }

    public String resolve(SpoolEvent event) {
        return routes.getOrDefault(event.getClass(), "spool");
    }
}
