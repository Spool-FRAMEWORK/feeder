package software.spool.janitor.api.strategy;

import software.spool.core.exception.SpoolException;
import software.spool.core.utils.polling.CancellationToken;

public interface JanitorStrategy {
    /**
     * Starts the strategy and returns a subscription that can be cancelled.
     *
     * @throws SpoolException if the strategy could not be started
     */
    void execute(CancellationToken token) throws SpoolException;
}
