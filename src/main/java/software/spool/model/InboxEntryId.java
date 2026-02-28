package software.spool.model;

public record InboxEntryId(String id) {
    public static InboxEntryId of(String id) {
        return new InboxEntryId(id);
    }
}
