package software.spool.feeder.api;

import software.spool.core.utils.CancellationToken;
import software.spool.core.utils.ErrorRouter;
import software.spool.feeder.api.strategy.FeederStrategy;

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
 * {@link software.spool.feeder.api.builder.FeederBuilderFactory}
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
 * @see software.spool.feeder.api.builder.FeederBuilderFactory
 */
public class Feeder {
    private final FeederStrategy strategy;
    private CancellationToken token;
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
        this.token  = CancellationToken.NONE;
        this.errorRouter = errorRouter;
    }

    /**
     * Starts the publishing process.
     *
     * <p>
     *
     * the resulting subscription. Calling this method when publishing is
     * already active has no effect. Any exceptions are routed through the
     * configured {@link ErrorRouter}.
     * </p>
     */
    public void startFeeding() {
        if (token.isActive()) return;
        token = CancellationToken.create();
        try {
            strategy.execute(token);
        } catch (Exception e) {
            errorRouter.dispatch(e);
        }
    }

    /**
     * Stops the publishing process.
     *
     * <p>
     * the subscription. Calling this method when publishing is already
     * stopped has no effect. Any exceptions are routed through the
     * configured {@link ErrorRouter}.
     * </p>
     */
    public void stopFeeding() {
        if (!token.isActive())
            return;
        try {
            token.cancel();
            token = CancellationToken.NONE;
        } catch (Exception e) {
            errorRouter.dispatch(e);
        }
    }
}
