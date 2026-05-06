package software.spool.janitor.internal.control;

import software.spool.core.adapter.logging.LoggerFactory;
import software.spool.core.model.EnvelopeStatus;
import software.spool.core.model.failure.EnvelopeQuarantined;
import software.spool.core.pipeline.PipelineContext;
import software.spool.core.pipeline.Step;
import software.spool.core.port.inbox.InboxUpdater;
import software.spool.core.port.logging.Logger;

import javax.management.AttributeNotFoundException;
import java.util.Collection;

public class QuarantineFailedEnvelopesStep implements Step<PipelineContext, PipelineContext> {
    private static final Logger LOG = LoggerFactory.getLogger(QuarantineFailedEnvelopesStep.class);
    private final InboxUpdater updater;

    public QuarantineFailedEnvelopesStep(InboxUpdater updater) {
        this.updater = updater;
    }

    @Override
    public PipelineContext apply(PipelineContext context) throws AttributeNotFoundException {
        Collection<EnvelopeQuarantined> events = context.require(JanitorScheduleKeys.ENVELOPES_QUARANTINED);
        updater.update(events.stream()
                .map(EnvelopeQuarantined::idempotencyKey).toList(), EnvelopeStatus.QUARANTINED);
        LOG.info("Quarantined {} envelopes", events.size());
        return context;
    }
}
