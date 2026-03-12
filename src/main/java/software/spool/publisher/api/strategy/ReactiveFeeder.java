package software.spool.publisher.api.strategy;

import software.spool.core.control.Handler;
import software.spool.core.model.InboxItemStored;
import software.spool.core.port.*;

/**
 * Reactive {@link FeederStrategy} that listens for {@link InboxItemStored}
 * events on the event bus and processes each one immediately.
 *
 * <p>
 * When started, this strategy subscribes to {@code InboxItemStored} events.
 * Each event triggers the handler which updates the inbox status to
 * {@code PUBLISHING}, emits an {@code ItemPublished} event, and marks the
 * item as {@code PUBLISHED}.
 * </p>
 *
 * <p>
 * This strategy is best suited for low-latency, event-driven architectures
 * where items should be published as soon as they arrive in the inbox.
 * </p>
 */
public class ReactiveFeeder implements FeederStrategy {
    private final EventBusListener eventBusListener;
    private final Handler<InboxItemStored> handler;

    /**
     * Creates a new reactive feeder.
     *
     * @param eventBusListener the event bus listener to subscribe with
     * @param handler          the handler that processes each inbox item
     */
    public ReactiveFeeder(EventBusListener eventBusListener, Handler<InboxItemStored> handler) {
        this.eventBusListener = eventBusListener;
        this.handler = handler;
    }

    /**
     * Subscribes to {@link InboxItemStored} events on the event bus.
     *
     * @return a {@link Subscription} that can be cancelled to stop publishing
     */
    @Override
    public Subscription start() {
        return eventBusListener.on(InboxItemStored.class, handler);
    }
}
