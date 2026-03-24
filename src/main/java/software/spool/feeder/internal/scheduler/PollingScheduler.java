package software.spool.feeder.internal.scheduler;

import software.spool.core.utils.CancellationToken;
import software.spool.feeder.api.utils.PollingPolicy;

public interface PollingScheduler {
    void schedule(Runnable task, PollingPolicy policy, CancellationToken token);
}
