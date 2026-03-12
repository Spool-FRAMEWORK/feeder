package software.spool.publisher.api.utils;

import software.spool.core.model.Event;

import java.util.HashMap;
import java.util.Map;

/**
 * Routes events to named channels based on their type.
 *
 * <p>
 * A {@code ChannelRouter} maintains a mapping from event types to channel
 * names. When {@link #resolve(Event)} is called, it returns the channel
 * registered for the event's class, or the default channel {@code "spool"}
 * if no mapping exists.
 * </p>
 *
 * <pre>{@code
 * ChannelRouter router = new ChannelRouter()
 *         .route(OrderReceived.class, "orders")
 *         .route(PaymentProcessed.class, "payments");
 *
 * String channel = router.resolve(event); // "orders", "payments", or "spool"
 * }</pre>
 */
public class ChannelRouter {
    private final Map<Class<? extends Event>, String> routes;

    public ChannelRouter() {
        routes = new HashMap<>();
    }

    /**
     * Registers a channel mapping for the given event type.
     *
     * @param <T>       the event type
     * @param eventType the event class to route; must not be {@code null}
     * @param channel   the target channel name; must not be {@code null}
     * @return this router for chaining
     */
    public <T extends Event> ChannelRouter route(Class<T> eventType, String channel) {
        routes.put(eventType, channel);
        return this;
    }

    /**
     * Resolves the channel for the given event.
     *
     * @param event the event to resolve; must not be {@code null}
     * @return the registered channel name, or {@code "spool"} if no mapping exists
     */
    public String resolve(Event event) {
        return routes.getOrDefault(event.getClass(), "spool");
    }
}
