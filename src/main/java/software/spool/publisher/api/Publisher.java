package software.spool.publisher.api;

public class Publisher {
    public static PublisherBuilder sender(String sender) {
        return new PublisherBuilder(sender);
    }
}
