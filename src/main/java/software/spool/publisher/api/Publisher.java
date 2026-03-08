package software.spool.publisher.api;

import software.spool.publisher.api.strategy.PublisherStrategy;
import software.spool.publisher.internal.port.Subscription;

import java.util.function.Supplier;

public class Publisher {
    private final PublisherStrategy strategy;
    private Subscription subscription;
    private final ErrorRouter errorRouter;

    public Publisher(PublisherStrategy strategy, ErrorRouter errorRouter) {
        this.strategy = strategy;
        this.subscription = Subscription.NULL;
        this.errorRouter = errorRouter;
    }

    public void startPublishing() {
        if (subscription.isActive()) return;
        try {
            subscription = strategy.start();
        } catch (Exception e) {
            errorRouter.dispatch(e);
        }
    }

    public void stopPublishing() {
        if (!subscription.isActive()) return;
        try {
            subscription = strategy.stop();
        } catch (Exception e) {
            errorRouter.dispatch(e);
        }
    }
}
