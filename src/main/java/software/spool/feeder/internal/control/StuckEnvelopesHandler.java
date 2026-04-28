package software.spool.feeder.internal.control;

import software.spool.core.model.event.EnvelopeStored;
import software.spool.core.model.vo.Envelope;
import software.spool.core.model.vo.EventMetadataKey;
import software.spool.core.port.bus.BrokerMessage;
import software.spool.core.port.bus.Destination;
import software.spool.core.port.bus.EventPublisher;
import software.spool.core.port.bus.Handler;
import software.spool.core.port.inbox.InboxUpdater;
import software.spool.core.utils.routing.ErrorRouter;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

public class StuckEnvelopesHandler implements Handler<Collection<Envelope>> {
    private final InboxUpdater updater;
    private final EventPublisher publisher;
    private final ErrorRouter errorRouter;
    private final Duration threshold;

    public StuckEnvelopesHandler(InboxUpdater updater, EventPublisher publisher, ErrorRouter errorRouter, Duration threshold) {
        this.updater = Objects.requireNonNull(updater);
        this.publisher = Objects.requireNonNull(publisher);
        this.errorRouter = Objects.requireNonNull(errorRouter);
        this.threshold = threshold;
    }

    @Override
    public void handle(Collection<Envelope> envelopes) {
        envelopes.stream()
                .filter(e -> e.capturedAt().isBefore(Instant.now().minus(threshold)))
                .map(Envelope::retry)
                .forEach(e -> {
                    Envelope envelope = updater.update(e);
                    if (envelope != null) return;
                    try {
                        EnvelopeStored event = buildEventFrom(e);
                        publish(event);
                    } catch (Exception ex) {
                        errorRouter.dispatch(ex);
                    }
                });
    }

    private void publish(EnvelopeStored event) {
        publisher.publish(new Destination("spool." + event.getClass().getSimpleName()),
                new BrokerMessage<>(event, EnvelopeStored.class.getSimpleName(), Map.of()));
    }

    private static EnvelopeStored buildEventFrom(Envelope e) {
        return EnvelopeStored.builder()
                .correlationId(e.metadata().get(EventMetadataKey.CORRELATION_ID))
                .idempotencyKey(e.idempotencyKey())
                .build();
    }
}
