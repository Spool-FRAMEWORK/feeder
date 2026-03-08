package software.spool.publisher.api.builder;

public class Publishers {
    public static PollingPublisherBuilder polling() {
        return PollingPublisherBuilder.create();
    }

    public static ReactivePublisherBuilder reactive() {
        return ReactivePublisherBuilder.create();
    }
}
