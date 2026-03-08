package software.spool.publisher.api.strategy;

import software.spool.publisher.internal.port.Subscription;

public interface PublisherStrategy {
    Subscription start();
    default Subscription stop() {
        return Subscription.NULL;
    }
}
