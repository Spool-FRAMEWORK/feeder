package software.spool.publisher.internal.control;

import software.spool.core.control.Handler;
import software.spool.core.model.*;
import software.spool.core.port.*;

import java.util.Objects;

/**
 * Handler that processes {@link InboxItemStored} events by updating the
 * inbox item status through a publishing lifecycle.
 *
 * <p>
 * The processing flow for each event:
 * </p>
 * <ol>
 * <li>Update the inbox item status to {@code PUBLISHING}.</li>
 * <li>Emit an {@link ItemPublished} event with the item payload.</li>
 * <li>Update the inbox item status to {@code PUBLISHED}.</li>
 * </ol>
 *
 * <p>
 * If the inbox item no longer exists (update returns {@code null}), the
 * event is silently skipped.
 * </p>
 */
public class InboxItemStoredHandler implements Handler<InboxItemStored> {
    private final InboxUpdater updater;
    private final EventBusEmitter emitter;

    /**
     * Creates a new handler with the given ports.
     *
     * @param updater the inbox updater for changing item statuses
     * @param emitter the event bus emitter for publishing events
     */
    public InboxItemStoredHandler(InboxUpdater updater, EventBusEmitter emitter) {
        this.updater = updater;
        this.emitter = emitter;
    }

    @Override
    public void handle(InboxItemStored object) {
        InboxItem item = updater.update(object.idempotencyKey(), InboxItemStatus.PUBLISHING);
        if (Objects.isNull(item))
            return;
        emitter.emit(ItemPublished.builder()
                .from(object)
                .payload(item.payload())
                .build());
        updater.update(object.idempotencyKey(), InboxItemStatus.PUBLISHED);
    }
}
