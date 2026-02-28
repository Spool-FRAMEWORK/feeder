package software.spool.publisher.internal.port;

import software.spool.model.InboxEntryId;
import software.spool.model.InboxEventStatus;

public interface InboxUpdater {
    void update(InboxEntryId id, InboxEventStatus status);
}
