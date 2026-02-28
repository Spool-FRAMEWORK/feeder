package software.spool.publisher.api;

import software.spool.model.RawDataWrittenToInbox;
import software.spool.publisher.internal.util.ChannelRouter;
import software.spool.publisher.internal.util.ErrorRouter;
import software.spool.publisher.internal.control.RawDataWrittenToInboxHandler;
import software.spool.publisher.internal.port.EventBus;
import software.spool.publisher.internal.port.InboxUpdater;

import java.util.Objects;

public class PublisherBuilder {
    private final String sender;
    private InboxUpdater updater;
    private EventBus bus;
    private ChannelRouter channelRouter;
    private ErrorRouter errorRouter;

    protected PublisherBuilder(String sender) {
        this.sender = sender;
    }

    public PublisherBuilder updater(InboxUpdater updater) {
        this.updater = updater;
        return this;
    }

    public PublisherBuilder on(EventBus bus) {
        this.bus = bus;
        return this;
    }

    public PublisherBuilder with(ChannelRouter channelRouter) {
        this.channelRouter = channelRouter;
        return this;
    }

    public PublisherBuilder with(ErrorRouter errorRouter) {
        this.errorRouter = errorRouter;
        return this;
    }

    public void build() {
        Objects.requireNonNull(bus, "bus required");
        Objects.requireNonNull(updater, "updater required");
        Objects.requireNonNull(channelRouter, "channelRouter required");
        Objects.requireNonNull(errorRouter, "errorRouter required");

        RawDataWrittenToInboxHandler handler = new RawDataWrittenToInboxHandler(
                updater, bus, channelRouter, errorRouter
        );

        bus.subscribe(RawDataWrittenToInbox.class, handler::handle);
    }
}
