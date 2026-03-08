package software.spool.example;

import software.spool.publisher.api.ErrorRouter;
import software.spool.publisher.api.Publisher;
import software.spool.publisher.api.builder.Publishers;

import java.time.Duration;

public class Main {
    public static void main(String[] args) {
        Publisher publisher = Publishers.polling()
                .each(Duration.ofSeconds(10))
                .withErrorRouter(new ErrorRouter())
                .build();
        publisher.startPublishing();
    }
}
