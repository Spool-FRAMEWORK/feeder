package software.spool.publisher.internal.util;

import software.spool.core.exception.DuplicateEventException;
import software.spool.core.exception.InboxWriteException;
import software.spool.core.model.IdempotencyKey;
import software.spool.core.model.InboxItemStatus;
import software.spool.publisher.api.InboxItem;
import software.spool.publisher.api.port.*;

import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class InMemoryInbox implements InboxReader, InboxUpdater {
    private final ConcurrentHashMap<IdempotencyKey, InboxItem> store = new ConcurrentHashMap<>();

    public void write(InboxItem item) throws InboxWriteException {
        InboxItem existing = store.putIfAbsent(item.idempotencyKey(), item);
        if (existing != null)
            throw new DuplicateEventException(item.idempotencyKey());
    }

    @Override
    public Stream<InboxItem> findByStatus(InboxItemStatus status) {
        return store.values().stream()
            .filter(item -> item.status() == status);
    }

    @Override
    public InboxItem update(IdempotencyKey key, InboxItemStatus status) {
        return store.computeIfPresent(key, (k, item) -> item.withStatus(status));
    }
}
