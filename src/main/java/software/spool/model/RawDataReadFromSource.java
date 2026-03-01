package software.spool.model;

import java.time.Instant;
import java.util.UUID;

public record RawDataReadFromSource(
        String eventId,
        String eventType,
        Instant timestamp,
        String sender,
        String payload
) implements SpoolEvent {

    public RawDataReadFromSource {
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
        private String sender;
        private String payload;

        public Builder sender(String sender) {
            this.sender = sender;
            return this;
        }

        public Builder payload(String payload) {
            this.payload = payload;
            return this;
        }

        public RawDataReadFromSource build() {
            if (sender == null || sender.isBlank()) {
                throw new IllegalArgumentException("sender is required");
            }

            return new RawDataReadFromSource(
                    UUID.randomUUID().toString(),
                    "RAW_DATA_READ_FROM_SOURCE",
                    Instant.now(),
                    sender,
                    payload
            );
        }
    }
}
