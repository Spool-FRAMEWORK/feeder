package software.spool.feeder.api.strategy;

import software.spool.core.exception.SpoolException;
import software.spool.core.port.Subscription;
import software.spool.core.utils.CancellationToken;

/**
 * Strategy interface that defines how the publisher discovers and processes
 * inbox items.
 *
 * <p>
 * Two built-in implementations are provided:
 * </p>
 * <ul>
 * <li>{@link ReactiveFeeder} — listens for {@code InboxItemStored} events
 * on the event bus.</li>
 * <li>{@link PollingFeeder} — polls the inbox at a fixed interval.</li>
 * </ul>
 *
 * @see ReactiveFeeder
 * @see PollingFeeder
 */
public interface FeederStrategy {
    /**
     * Starts the strategy and returns a subscription that can be cancelled.
     *
     * @throws SpoolException if the strategy could not be started
     */
    void execute(CancellationToken token) throws SpoolException;
}
