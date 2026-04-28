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
import software.spool.feeder.api.Janitor;
import software.spool.feeder.api.strategy.PollingJanitorStrategy;
import software.spool.feeder.api.utils.JanitorErrorRouter;
import software.spool.feeder.internal.control.PersistedEnvelopesHandler;
import software.spool.feeder.internal.control.QuarantineEnvelopesHandler;
import software.spool.feeder.internal.control.StuckEnvelopesHandler;
import software.spool.feeder.internal.port.decorator.SafeInboxStatusQuery;

import java.time.Duration;
import java.util.Collection;
import java.util.Objects;

public class PollingJanitorBuilder {
    private final ModuleHeartBeat heartBeat;
    private InboxStatusQuery reader;
    private InboxUpdater updater;
    private InboxEnvelopeRemover remover;
    private EventPublisher publisher;
    private EventSubscriber subscriber;
    private PollingConfiguration pollingConfiguration;
    private ErrorRouter errorRouter;
    private Integer millisecondsThreshold;

    PollingJanitorBuilder(ModuleHeartBeat heartBeat) {
        this.heartBeat = heartBeat;
    }

    public PollingJanitorBuilder from(InboxStatusQuery reader) {
        this.reader = SafeInboxStatusQuery.of(reader);
        return this;
    }

    public PollingJanitorBuilder with(InboxUpdater updater) {
        this.updater = SafeInboxUpdater.of(updater);
        return this;
    }

    public PollingJanitorBuilder removeWith(InboxEnvelopeRemover remover) {
        this.remover = remover;
        return this;
    }

    public PollingJanitorBuilder on(EventPublisher publisher) {
        this.publisher = SafeEventPublisher.of(publisher);
        return this;
    }

    public PollingJanitorBuilder subscribeWith(EventSubscriber subscriber) {
        this.subscriber = SafeEventSubscriber.of(subscriber);
        return this;
    }

    public PollingJanitorBuilder every(Duration interval) {
        this.pollingConfiguration = PollingConfiguration.every(interval);
        return this;
    }

    public PollingJanitorBuilder withErrorRouter(ErrorRouter errorRouter) {
        this.errorRouter = errorRouter;
        return this;
    }

    public PollingJanitorBuilder withMillisecondsThreshold(Integer millisecondsThreshold) {
        this.millisecondsThreshold = millisecondsThreshold;
        return this;
    }

    public Janitor create() {
        return new Janitor(initializeStrategy(), getErrorRouter(), heartBeat);
    }

    private ErrorRouter getErrorRouter() {
        return Objects.requireNonNullElse(errorRouter, JanitorErrorRouter.defaults(publisher));
    }

    private PollingJanitorStrategy initializeStrategy() {
        return new PollingJanitorStrategy(reader, subscriber, initializePersistedHandler(), initialiazeQuarantineHandler(), initializeHandler(), pollingConfiguration);
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
