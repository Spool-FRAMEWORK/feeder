package software.spool.model;

public interface Handler<T extends SpoolEvent> {
    void handle(T event);
}
