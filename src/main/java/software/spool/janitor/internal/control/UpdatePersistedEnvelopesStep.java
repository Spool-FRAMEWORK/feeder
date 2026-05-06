package software.spool.janitor.internal.control;

import software.spool.core.adapter.logging.LoggerFactory;
import software.spool.core.model.EnvelopeStatus;
import software.spool.core.model.event.EnvelopePersisted;
import software.spool.core.model.vo.Envelope;
import software.spool.core.pipeline.PipelineContext;
import software.spool.core.pipeline.Step;
import software.spool.core.port.inbox.InboxUpdater;
import software.spool.core.port.logging.Logger;

import javax.management.AttributeNotFoundException;
import java.util.Collection;

public class UpdatePersistedEnvelopesStep implements Step<PipelineContext, PipelineContext> {
    private static final Logger LOG = LoggerFactory.getLogger(UpdatePersistedEnvelopesStep.class);
    private final InboxUpdater updater;

    public UpdatePersistedEnvelopesStep(InboxUpdater updater) {
        this.updater = updater;
    }

    @Override
    public PipelineContext apply(PipelineContext context) throws AttributeNotFoundException {
        Collection<Envelope> result = updater.update(context.require(JanitorScheduleKeys.ENVELOPES_PERSISTED).stream()
                .map(EnvelopePersisted::idempotencyKey).toList(), EnvelopeStatus.PERSISTED);
        LOG.info("Updated {} persisted envelopes from inbox", result.size());
        return context;
    }
}
