package software.spool.example;

import software.spool.publisher.api.builder.PollingPublisherBuilder;
import software.spool.publisher.api.builder.Publishers;

public class Main {
    public static void main(String[] args) {
        PollingPublisherBuilder polling = Publishers.polling();
    }
}
