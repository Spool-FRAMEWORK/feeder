package software.spool.feeder.internal.control;

import software.spool.core.adapter.jackson.PayloadDeserializerFactory;
import software.spool.core.model.EnvelopeStatus;
import software.spool.core.model.event.EnvelopeStored;
import software.spool.core.model.vo.Envelope;
import software.spool.core.model.vo.EventMetadataKey;
import software.spool.core.model.vo.PartitionKeySchema;
import software.spool.core.port.bus.BrokerMessage;
import software.spool.core.port.bus.Destination;
import software.spool.core.port.bus.EventPublisher;
import software.spool.core.port.bus.Handler;
import software.spool.core.port.inbox.InboxUpdater;
import software.spool.core.utils.routing.ErrorRouter;

import java.util.Map;
import java.util.Objects;

public class EnvelopeStoredHandler implements Handler<EnvelopeStored> {
    private final InboxUpdater updater;
    private final EventPublisher publisher;
    private final ErrorRouter errorRouter;

    public EnvelopeStoredHandler(InboxUpdater updater, EventPublisher publisher, ErrorRouter errorRouter) {
        this.updater = Objects.requireNonNull(updater);
        this.publisher = Objects.requireNonNull(publisher);
        this.errorRouter = Objects.requireNonNull(errorRouter);
    }

    @Override
    public void handle(EnvelopeStored envelopeStored) {
        Envelope envelope = updater.update(envelopeStored.idempotencyKey(), EnvelopeStatus.CAPTURED);
        if (Objects.isNull(envelope)) return;
        try {
            EnvelopeStored event = EnvelopeStored.builder()
                    .from(envelopeStored)
                    .build();
            publisher.publish(new Destination("spool." + event.getClass().getSimpleName()),
                    new BrokerMessage<>(event, event.getClass().getSimpleName(), Map.of()));
            updater.update(envelopeStored.idempotencyKey(), EnvelopeStatus.CAPTURED);
        } catch (Exception e) {
            errorRouter.dispatch(e, envelopeStored);
        }
    }

    private static PartitionKeySchema getPartitionKeySchema(Envelope envelope) {
        return PayloadDeserializerFactory.json().as(PartitionKeySchema.class)
                .deserialize(envelope.metadata().get(EventMetadataKey.PARTITION_SCHEMA));
    }
}
