package software.spool.publisher.internal.control;

import software.spool.model.*;
import software.spool.model.InboxEventStatus;
import software.spool.publisher.internal.port.EventBus;
import software.spool.publisher.internal.port.InboxUpdater;
import software.spool.publisher.api.ChannelRouter;
import software.spool.publisher.api.ErrorRouter;

public class RawDataWrittenToInboxHandler implements Handler<RawDataWrittenToInbox> {
    private final InboxUpdater updater;
    private final EventBus bus;
    private final ChannelRouter channelRouter;
    private final ErrorRouter errorRouter;

    public RawDataWrittenToInboxHandler(InboxUpdater updater, EventBus bus, ChannelRouter channelRouter, ErrorRouter errorRouter) {
        this.updater = updater;
        this.bus = bus;
        this.channelRouter = channelRouter;
        this.errorRouter = errorRouter;
    }

    @Override
    public void handle(RawDataWrittenToInbox event) {
        try {
            bus.emit(RawDataPublished.with(event.payload()), channelRouter.resolve(event));
            updater.update(InboxEntryId.of(event.idempotencyKey()), InboxEventStatus.PUBLISHED);
        } catch (Exception e) {
            errorRouter.dispatch(e);
        }
    }
}
