package software.spool.feeder.internal.control;

import software.spool.core.exception.SpoolException;
import software.spool.core.model.event.EnvelopePersisted;
import software.spool.core.port.bus.Handler;
import software.spool.core.port.inbox.InboxEnvelopeRemover;
import software.spool.core.utils.routing.ErrorRouter;

import java.util.Collection;

public class PersistedEnvelopesHandler implements Handler<Collection<EnvelopePersisted>> {
    private final InboxEnvelopeRemover remover;
    private final ErrorRouter errorRouter;

    public PersistedEnvelopesHandler(InboxEnvelopeRemover remover, ErrorRouter errorRouter) {
        this.remover = remover;
        this.errorRouter = errorRouter;
    }

    @Override
    public void handle(Collection<EnvelopePersisted> envelopePersistedEvents) throws SpoolException {
        try {
            envelopePersistedEvents.stream().map(EnvelopePersisted::idempotencyKey).forEach(remover::remove);
        } catch (Exception e) {
            errorRouter.dispatch(e);
        }
    }
}
