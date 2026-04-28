package software.spool.feeder.api.builder;

import software.spool.core.model.event.EnvelopePersisted;
import software.spool.core.model.failure.EnvelopeQuarantined;
import software.spool.core.port.bus.EventPublisher;
import software.spool.core.port.bus.EventSubscriber;
import software.spool.core.port.bus.Handler;
import software.spool.core.port.decorator.SafeEventPublisher;
import software.spool.core.port.decorator.SafeEventSubscriber;
import software.spool.core.port.decorator.SafeInboxUpdater;
import software.spool.core.port.inbox.InboxEnvelopeRemover;
import software.spool.core.port.inbox.InboxStatusQuery;
import software.spool.core.port.inbox.InboxUpdater;
import software.spool.core.port.watchdog.ModuleHeartBeat;
import software.spool.core.utils.polling.PollingConfiguration;
import software.spool.core.utils.routing.ErrorRouter;
import software.spool.feeder.api.Feeder;
import software.spool.feeder.api.strategy.PollingFeederStrategy;
import software.spool.feeder.api.utils.FeederErrorRouter;
import software.spool.feeder.internal.control.PersistedEnvelopesHandler;
import software.spool.feeder.internal.control.QuarantineEnvelopesHandler;
import software.spool.feeder.internal.control.StuckEnvelopesHandler;
import software.spool.feeder.internal.port.decorator.SafeInboxStatusQuery;

import java.time.Duration;
import java.util.Collection;
import java.util.Objects;

public class PollingFeederBuilder {
    private final ModuleHeartBeat heartBeat;
    private InboxStatusQuery reader;
    private InboxUpdater updater;
    private InboxEnvelopeRemover remover;
    private EventPublisher publisher;
    private EventSubscriber subscriber;
    private PollingConfiguration pollingConfiguration;
    private ErrorRouter errorRouter;
    private Integer millisecondsThreshold;

    PollingFeederBuilder(ModuleHeartBeat heartBeat) {
        this.heartBeat = heartBeat;
    }

    public PollingFeederBuilder from(InboxStatusQuery reader) {
        this.reader = SafeInboxStatusQuery.of(reader);
        return this;
    }

    public PollingFeederBuilder with(InboxUpdater updater) {
        this.updater = SafeInboxUpdater.of(updater);
        return this;
    }

    public PollingFeederBuilder removeWith(InboxEnvelopeRemover remover) {
        this.remover = remover;
        return this;
    }

    public PollingFeederBuilder on(EventPublisher publisher) {
        this.publisher = SafeEventPublisher.of(publisher);
        return this;
    }

    public PollingFeederBuilder subscribeWith(EventSubscriber subscriber) {
        this.subscriber = SafeEventSubscriber.of(subscriber);
        return this;
    }

    public PollingFeederBuilder every(Duration interval) {
        this.pollingConfiguration = PollingConfiguration.every(interval);
        return this;
    }

    public PollingFeederBuilder withErrorRouter(ErrorRouter errorRouter) {
        this.errorRouter = errorRouter;
        return this;
    }

    public PollingFeederBuilder withMillisecondsThreshold(Integer millisecondsThreshold) {
        this.millisecondsThreshold = millisecondsThreshold;
        return this;
    }

    public Feeder create() {
        return new Feeder(initializeStrategy(), getErrorRouter(), heartBeat);
    }

    private ErrorRouter getErrorRouter() {
        return Objects.requireNonNullElse(errorRouter, FeederErrorRouter.defaults(publisher));
    }

    private PollingFeederStrategy initializeStrategy() {
        return new PollingFeederStrategy(reader, subscriber, initializePersistedHandler(), initialiazeQuarantineHandler(), initializeHandler(), pollingConfiguration);
    }

    private Handler<Collection<EnvelopeQuarantined>> initialiazeQuarantineHandler() {
        return new QuarantineEnvelopesHandler(updater, getErrorRouter());
    }

    private Handler<Collection<EnvelopePersisted>> initializePersistedHandler() {
        return new PersistedEnvelopesHandler(remover, getErrorRouter());
    }

    private StuckEnvelopesHandler initializeHandler() {
        return new StuckEnvelopesHandler(updater, publisher, getErrorRouter(), Duration.ofMillis(Objects.requireNonNullElse(millisecondsThreshold, 5000)));
    }
}
