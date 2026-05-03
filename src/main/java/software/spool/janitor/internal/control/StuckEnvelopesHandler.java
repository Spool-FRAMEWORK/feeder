package software.spool.janitor.internal.control;

import software.spool.core.adapter.logging.LoggerFactory;
import software.spool.core.model.event.EnvelopeStored;
import software.spool.core.model.vo.Envelope;
import software.spool.core.model.vo.EventMetadataKey;
import software.spool.core.port.bus.EventPublisher;
import software.spool.core.port.bus.Handler;
import software.spool.core.port.inbox.InboxUpdater;
import software.spool.core.port.logging.Logger;
import software.spool.core.utils.routing.ErrorRouter;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Objects;

public class StuckEnvelopesHandler implements Handler<Collection<Envelope>> {
    private static final Logger LOG = LoggerFactory.getLogger(StuckEnvelopesHandler.class);
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
                        publisher.publish(event);
                        LOG.warn("Republished Envelope {} | current attempt: {}", event, e.retries());
                    } catch (Exception ex) {
                        errorRouter.dispatch(ex);
                    }
                });
    }

    private static EnvelopeStored buildEventFrom(Envelope e) {
        return EnvelopeStored.builder()
                .correlationId(e.metadata().get(EventMetadataKey.CORRELATION_ID))
                .idempotencyKey(e.idempotencyKey())
                .build();
    }
}
