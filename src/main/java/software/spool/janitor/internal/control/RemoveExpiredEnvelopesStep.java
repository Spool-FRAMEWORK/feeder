package software.spool.janitor.internal.control;

import software.spool.core.model.EnvelopeStatus;
import software.spool.core.model.vo.Envelope;
import software.spool.core.pipeline.Step;
import software.spool.core.pipeline.PipelineContext;
import software.spool.core.port.inbox.InboxEnvelopeRemover;
import software.spool.core.port.inbox.InboxStatusQuery;
import software.spool.core.utils.routing.ErrorRouter;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

public class RemoveExpiredEnvelopesStep implements Step<PipelineContext, PipelineContext> {
    private final ErrorRouter errorRouter;
    private final Duration ttl;
    private final InboxEnvelopeRemover remover;
    private final InboxStatusQuery reader;

    public RemoveExpiredEnvelopesStep(ErrorRouter errorRouter, Duration ttl, InboxEnvelopeRemover remover, InboxStatusQuery reader) {
        this.errorRouter = errorRouter;
        this.ttl = Objects.requireNonNullElse(ttl, Duration.ofDays(1));
        this.remover = remover;
        this.reader = reader;
    }

    @Override
    public PipelineContext apply(PipelineContext context) {
        try {
            reader.findByStatus(EnvelopeStatus.PERSISTED).stream()
                    .filter(e -> getLastModifiedInstant(e).isBefore(Instant.now().minus(ttl)))
                    .map(Envelope::idempotencyKey)
                    .forEach(remover::remove);
            return context;
        } catch (Exception e) {
            errorRouter.dispatch(e);
            throw e;
        }
    }

    private Instant getLastModifiedInstant(Envelope envelope) {
        return Objects.isNull(envelope.updatedAt()) ? envelope.capturedAt() : envelope.updatedAt();
    }
}
