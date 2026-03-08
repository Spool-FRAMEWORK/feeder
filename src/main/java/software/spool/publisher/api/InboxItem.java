package software.spool.publisher.api;

import software.spool.core.model.IdempotencyKey;
import software.spool.core.model.InboxItemStatus;

import java.time.Instant;

public record InboxItem(
        IdempotencyKey idempotencyKey,
        String payload,
        InboxItemStatus status,
        Instant timestamp
) {
    public InboxItem withStatus(InboxItemStatus newStatus) {
        return new InboxItem(idempotencyKey, payload, newStatus, timestamp);
    }
}
