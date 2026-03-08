package software.spool.publisher.api.strategy;

import software.spool.core.control.Handler;
import software.spool.core.model.InboxItemStatus;
import software.spool.publisher.api.InboxItem;
import software.spool.publisher.api.port.InboxReader;
import software.spool.publisher.internal.port.Subscription;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.*;

public class PollingPublisher implements PublisherStrategy {
    private final InboxReader reader;
    private final Handler<InboxItem> handler;
    private final Duration interval;

    public PollingPublisher(InboxReader reader, Handler<InboxItem> handler, Duration interval) {
        this.reader = reader;
        this.handler = handler;
        this.interval = Objects.requireNonNullElse(interval, Duration.ofSeconds(30));
    }

    @Override
    public Subscription start() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleWithFixedDelay(
                () -> reader.findByStatus(InboxItemStatus.PUBLISHING).forEach(handler::handle),
                0, interval.toMillis(), TimeUnit.MILLISECONDS
        );
        return new Subscription() {
            public void cancel()    { scheduler.shutdown(); }
            public boolean isActive() { return !scheduler.isShutdown(); }
        };
    }
}
