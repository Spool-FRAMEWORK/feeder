package software.spool.publisher.api;

import software.spool.core.utils.ErrorRouter;
import software.spool.publisher.api.strategy.FeederStrategy;
import software.spool.core.port.Subscription;

/**
 * Main API entry point for the publishing lifecycle.
 *
 * <p>
 * A {@code Feeder} wraps a {@link FeederStrategy} and manages start/stop
 * semantics with built-in error routing. It acts as the bridge between the
 * inbox and downstream event channels.
 * </p>
 *
 * <p>
 * Use the fluent builders in
 * {@link software.spool.publisher.api.builder.FeederBuilderFactory}
 * to construct instances:
 * </p>
 *
 * <pre>{@code
 * Feeder feeder = FeederBuilderFactory.reactive()
 *         .from(eventBusListener)
 *         .with(inboxUpdater)
 *         .on(eventBusEmitter)
 *         .withErrorRouter(errorRouter)
 *         .create();
 *
 * feeder.startPublishing();
 * }</pre>
 *
 * @see FeederStrategy
 * @see software.spool.publisher.api.builder.FeederBuilderFactory
 */
public class Feeder {
    private final FeederStrategy strategy;
    private Subscription subscription;
    private final ErrorRouter errorRouter;

    /**
     * Creates a new {@code Feeder} with the given strategy and error router.
     *
     * @param strategy    the publishing strategy to use; must not be {@code null}
     * @param errorRouter the error router for handling exceptions; must not be
     *                    {@code null}
     */
    public Feeder(FeederStrategy strategy, ErrorRouter errorRouter) {
        this.strategy = strategy;
        this.subscription = Subscription.NULL;
        this.errorRouter = errorRouter;
    }

    /**
     * Starts the publishing process.
     *
     * <p>
     * Delegates to the underlying {@link FeederStrategy#start()} and stores
     * the resulting subscription. Calling this method when publishing is
     * already active has no effect. Any exceptions are routed through the
     * configured {@link ErrorRouter}.
     * </p>
     */
    public void startPublishing() {
        if (subscription.isActive())
            return;
        try {
            subscription = strategy.start();
        } catch (Exception e) {
            errorRouter.dispatch(e);
        }
    }

    /**
     * Stops the publishing process.
     *
     * <p>
     * Delegates to the underlying {@link FeederStrategy#stop()} and clears
     * the subscription. Calling this method when publishing is already
     * stopped has no effect. Any exceptions are routed through the
     * configured {@link ErrorRouter}.
     * </p>
     */
    public void stopPublishing() {
        if (!subscription.isActive())
            return;
        try {
            subscription = strategy.stop();
        } catch (Exception e) {
            errorRouter.dispatch(e);
        }
    }
}
