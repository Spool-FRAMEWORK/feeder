package software.spool.publisher.internal.port;

public interface Subscription {
    Subscription NULL = new Subscription() {
        @Override public void cancel() {}
        @Override public boolean isActive() { return false; }
    };

    void cancel();
    boolean isActive();
}
