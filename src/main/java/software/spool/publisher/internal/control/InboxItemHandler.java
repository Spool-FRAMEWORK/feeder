package software.spool.publisher.internal.control;

import software.spool.core.control.Handler;
import software.spool.core.exception.SpoolException;
import software.spool.core.model.InboxItemStatus;
import software.spool.core.model.ItemPublished;
import software.spool.publisher.api.InboxItem;
import software.spool.publisher.api.port.EventBusEmitter;
import software.spool.publisher.api.port.InboxUpdater;

import java.util.Objects;

public class InboxItemHandler implements Handler<InboxItem> {
    private final EventBusEmitter emitter;
    private final InboxUpdater updater;

    public InboxItemHandler(EventBusEmitter emitter, InboxUpdater updater) {
        this.emitter = emitter;
        this.updater = updater;
    }

    @Override
    public void handle(InboxItem object) throws SpoolException {
        InboxItem item = updater.update(object.idempotencyKey(), InboxItemStatus.PUBLISHING);
        if (Objects.isNull(item)) return;
        emitter.emit(ItemPublished.builder()
                .idempotencyKey(object.idempotencyKey())
                .payload(item.payload())
                .build());
        updater.update(object.idempotencyKey(), InboxItemStatus.PUBLISHED);
    }
}

