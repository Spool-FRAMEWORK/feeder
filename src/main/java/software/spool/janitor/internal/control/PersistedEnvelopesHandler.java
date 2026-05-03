package software.spool.janitor.internal.control;

import software.spool.core.adapter.logging.LoggerFactory;
import software.spool.core.exception.SpoolException;
import software.spool.core.model.event.EnvelopePersisted;
import software.spool.core.model.vo.Envelope;
import software.spool.core.port.bus.Handler;
import software.spool.core.port.inbox.InboxEnvelopeRemover;
import software.spool.core.port.logging.Logger;
import software.spool.core.utils.routing.ErrorRouter;

import java.util.Collection;

public class PersistedEnvelopesHandler implements Handler<Collection<EnvelopePersisted>> {
    private static final Logger LOG = LoggerFactory.getLogger(PersistedEnvelopesHandler.class);
    private final InboxEnvelopeRemover remover;
    private final ErrorRouter errorRouter;

    public PersistedEnvelopesHandler(InboxEnvelopeRemover remover, ErrorRouter errorRouter) {
        this.remover = remover;
        this.errorRouter = errorRouter;
    }

    @Override
    public void handle(Collection<EnvelopePersisted> envelopePersistedEvents) throws SpoolException {
        try {
            Collection<Envelope> result = remover.remove(envelopePersistedEvents.stream().map(EnvelopePersisted::idempotencyKey).toList());
            LOG.info("Removed {} persisted envelopes from inbox", result.size());
        } catch (Exception e) {
            errorRouter.dispatch(e);
        }
    }
}
