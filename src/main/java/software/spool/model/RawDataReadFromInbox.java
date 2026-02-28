package software.spool.model;

import java.time.Instant;
import java.util.UUID;

public record RawDataReadFromInbox(
        String eventId,
        String eventType,
        Instant timestamp,
        String sender,
        String payload
) implements SpoolEvent {

    public RawDataReadFromInbox {
        if (sender == null || sender.isBlank()) {
            throw new IllegalArgumentException("sender is required");
        }
        if (eventType == null || eventType.isBlank()) {
            throw new IllegalArgumentException("eventType is required");
        }
        if (eventId == null || eventId.isBlank()) {
            throw new IllegalArgumentException("eventId is required");
        }
        if (timestamp == null) {
            throw new IllegalArgumentException("timestamp is required");
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String sourceId;
        private String payload ;

        public Builder sourceId(String sourceId) {
            this.sourceId = sourceId;
            return this;
        }

        public Builder payload(String payload) {
            this.payload = payload;
            return this;
        }

        public RawDataReadFromInbox build() {
            if (sourceId == null || sourceId.isBlank()) {
                throw new IllegalArgumentException("sender is required");
            }

            String eventId = UUID.randomUUID().toString();
            String eventType = "RAW_INBOX_ITEM";
            Instant timestamp = Instant.now();

            return new RawDataReadFromInbox(
                    eventId,
                    eventType,
                    timestamp,
                    sourceId,
                    payload
            );
        }
    }
}
