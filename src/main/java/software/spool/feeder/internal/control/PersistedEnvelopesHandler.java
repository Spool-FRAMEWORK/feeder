package software.spool.feeder.internal.control;

import software.spool.core.exception.SpoolException;
import software.spool.core.model.vo.IdempotencyKey;
import software.spool.core.port.bus.Handler;
import software.spool.core.port.inbox.InboxEnvelopeRemover;
import software.spool.core.utils.routing.ErrorRouter;

import java.util.Collection;

public class PersistedEnvelopesHandler implements Handler<Collection<IdempotencyKey>> {
    private final InboxEnvelopeRemover remover;
    private final ErrorRouter errorRouter;

    public PersistedEnvelopesHandler(InboxEnvelopeRemover remover, ErrorRouter errorRouter) {
        this.remover = remover;
        this.errorRouter = errorRouter;
    }

    @Override
    public void handle(Collection<IdempotencyKey> idempotencyKeys) throws SpoolException {
        try {
            idempotencyKeys.forEach(remover::remove);
        } catch (Exception e) {
            errorRouter.dispatch(e);
        }
    }
}
