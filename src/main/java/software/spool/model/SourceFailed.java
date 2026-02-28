package software.spool.model;

import java.time.Instant;

public record SourceFailed(
        String eventId,
        String eventType,
        Instant timestamp,
        String errorMessage
) implements SpoolEvent {
}
