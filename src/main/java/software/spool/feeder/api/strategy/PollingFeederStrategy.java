package software.spool.feeder.api.strategy;

import software.spool.core.model.EnvelopeStatus;
import software.spool.core.model.event.EnvelopeStored;
import software.spool.core.model.vo.Envelope;
import software.spool.core.port.bus.Handler;
import software.spool.core.port.inbox.InboxStatusQuery;
import software.spool.core.utils.polling.CancellationToken;
import software.spool.core.utils.polling.PollingConfiguration;

import java.time.Duration;
import java.util.Collection;
import java.util.Objects;

public class PollingFeederStrategy implements FeederStrategy {
    private final InboxStatusQuery reader;
    private final Handler<Collection<Envelope>> handler;
    private final PollingConfiguration pollingConfiguration;

    public PollingFeederStrategy(InboxStatusQuery reader, Handler<Collection<Envelope>> handler, PollingConfiguration pollingConfiguration) {
        this.reader = Objects.requireNonNull(reader);
        this.handler = Objects.requireNonNull(handler);
        this.pollingConfiguration = Objects.requireNonNullElse(pollingConfiguration, PollingConfiguration.every(Duration.ofSeconds(10)));
    }

    @Override
    public void execute(CancellationToken token) {
        pollingConfiguration.scheduler().schedule(
                () -> handler.handle(reader.findByStatus(EnvelopeStatus.CAPTURED)),
                pollingConfiguration.policy(),
                token
        );
    }
}
