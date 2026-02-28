package software.spool.model;

import java.time.Instant;
import java.util.UUID;

public record RawDataPublished(
        String eventId,
        String eventType,
        Instant timestamp,
        String payload
) implements SpoolEvent {
    public static RawDataPublished with(String payload) {
        return new RawDataPublished(UUID.randomUUID().toString(), "RAW_DATA_PUBLISHED", Instant.now(), payload);
    }
}
