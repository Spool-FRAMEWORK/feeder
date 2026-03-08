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
        execute(strategy::start);
    }

    public void stopPublishing() {
        execute(strategy::stop);
    }

    private void execute(Supplier<Subscription> action) {
        if (!subscription.isActive()) return;
        try {
            subscription = action.get();
        } catch (Exception e) {
            errorRouter.dispatch(e);
        }
    }
}
