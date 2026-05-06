package software.spool.janitor.internal.control;

import software.spool.core.model.EnvelopeStatus;
import software.spool.core.model.vo.Envelope;
import software.spool.core.pipeline.Step;
import software.spool.core.pipeline.PipelineContext;
import software.spool.core.port.inbox.InboxEnvelopeRemover;
import software.spool.core.port.inbox.InboxStatusQuery;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

public class RemoveExpiredEnvelopesStep implements Step<PipelineContext, PipelineContext> {
    private final Duration ttl;
    private final InboxEnvelopeRemover remover;
    private final InboxStatusQuery reader;

    public RemoveExpiredEnvelopesStep(Duration ttl, InboxEnvelopeRemover remover, InboxStatusQuery reader) {
        this.ttl = Objects.requireNonNullElse(ttl, Duration.ofDays(1));
        this.remover = remover;
        this.reader = reader;
    }

    @Override
    public PipelineContext apply(PipelineContext context) {
        reader.findByStatus(EnvelopeStatus.PERSISTED).stream()
                .filter(e -> e.updatedAt().isBefore(Instant.now().minus(ttl)))
                .map(Envelope::idempotencyKey)
                .forEach(remover::remove);
        return context;
    }
}
