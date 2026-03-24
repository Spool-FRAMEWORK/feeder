package software.spool.feeder.api.builder;

/**
 * Factory entry point for creating pre-configured feeder builders.
 *
 * <p>
 * Two strategies are available:
 * </p>
 * <ul>
 * <li>{@link #polling()} — creates a polling-based feeder that queries the
 * inbox
 * at a fixed interval.</li>
 * <li>{@link #reactive()} — creates a reactive feeder that listens for
 * {@code InboxItemStored} events on the event bus.</li>
 * </ul>
 *
 * <pre>{@code
 * Feeder feeder = FeederBuilderFactory.reactive()
 *         .from(eventBusListener)
 *         .with(inboxUpdater)
 *         .on(eventBusEmitter)
 *         .withErrorRouter(errorRouter)
 *         .create();
 * }</pre>
 *
 * @see PollingFeederBuilder
 * @see ReactiveFeederBuilder
 */
public class FeederBuilderFactory {
    /**
     * Creates a builder for a polling-based feeder.
     *
     * @return a new {@link PollingFeederBuilder}
     */
    public static PollingFeederBuilder polling() {
        return new PollingFeederBuilder();
    }

    /**
     * Creates a builder for a reactive (event-driven) feeder.
     *
     * @return a new {@link ReactiveFeederBuilder}
     */
    public static ReactiveFeederBuilder reactive() {
        return new ReactiveFeederBuilder();
    }
}
