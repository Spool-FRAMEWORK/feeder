package software.spool.publisher.api.strategy;

import software.spool.core.control.Handler;
import software.spool.core.model.InboxItemStored;
import software.spool.publisher.api.port.EventBusListener;
import software.spool.publisher.internal.port.Subscription;

public class ReactivePublisher implements PublisherStrategy {
    private final EventBusListener eventBusListener;
    private final Handler<InboxItemStored> handler;

    public ReactivePublisher(EventBusListener eventBusListener, Handler<InboxItemStored> handler) {
        this.eventBusListener = eventBusListener;
        this.handler = handler;
    }

    @Override
    public Subscription start() {
        return eventBusListener.on(InboxItemStored.class, handler);
    }
}
