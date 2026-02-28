package software.spool.model;

import java.time.Instant;
import java.util.UUID;

public record RawDataPublishFailed(
        String eventId,
        String eventType,
        Instant timestamp,
        String sender,
        String idempotencyKey,
        String errorMessage,
        String payload
) implements SpoolEvent {
    public static RawDataPublishFailed from(RawDataWrittenToInbox origin, String reason) {
        return new RawDataPublishFailed(
                UUID.randomUUID().toString(),
                "RAW_DATA_PUBLISH_FAILED",
                Instant.now(),
                origin.sender(),
                origin.idempotencyKey(),
                reason,
                origin.payload()
        );
    }
}
