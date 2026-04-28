package software.spool.feeder.api.strategy;

import software.spool.core.model.EnvelopeStatus;
import software.spool.core.model.event.EnvelopeStored;
import software.spool.core.model.vo.Envelope;
import software.spool.core.port.bus.Handler;
import software.spool.core.port.inbox.InboxStatusQuery;
import software.spool.core.utils.polling.CancellationToken;
import software.spool.core.utils.polling.PollingConfiguration;

import java.time.Duration;
import java.util.Objects;

public class PollingFeederStrategy implements FeederStrategy {
    private final InboxStatusQuery reader;
    private final Handler<EnvelopeStored> handler;
    private final PollingConfiguration pollingConfiguration;

    public PollingFeederStrategy(InboxStatusQuery reader, Handler<EnvelopeStored> handler, PollingConfiguration pollingConfiguration) {
        this.reader = Objects.requireNonNull(reader);
        this.handler = Objects.requireNonNull(handler);
        this.pollingConfiguration = Objects.requireNonNullElse(pollingConfiguration, PollingConfiguration.every(Duration.ofSeconds(10)));
    }

    @Override
    public void execute(CancellationToken token) {
        pollingConfiguration.scheduler().schedule(
                () -> reader.findByStatus(EnvelopeStatus.CAPTURED).stream().map(this::toEvent).forEach(handler::handle),
                pollingConfiguration.policy(),
                token
        );
    }

    private EnvelopeStored toEvent(Envelope envelope) {
        return EnvelopeStored.builder()
                .idempotencyKey(envelope.idempotencyKey())
                .build();
    }
}
