package software.spool.janitor.api.builder;

import software.spool.core.pipeline.ObservedStep;
import software.spool.core.pipeline.Pipeline;
import software.spool.core.pipeline.PipelineContext;
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
import software.spool.janitor.api.Janitor;
import software.spool.janitor.api.strategy.PollingJanitorStrategy;
import software.spool.janitor.api.utils.JanitorErrorRouter;
import software.spool.janitor.internal.control.*;
import software.spool.janitor.internal.port.decorator.SafeInboxStatusQuery;

import java.time.Duration;
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
    private Integer millisecondsTtl;

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

    public PollingJanitorBuilder withMillisecondsTtl(Integer millisecondsTtl) {
        this.millisecondsTtl = millisecondsTtl;
        return this;
    }

    public Janitor create() {
        return new Janitor(initializeStrategy(), getErrorRouter(), heartBeat);
    }

    private ErrorRouter getErrorRouter() {
        return Objects.requireNonNullElse(errorRouter, JanitorErrorRouter.defaults(publisher));
    }

    private PollingJanitorStrategy initializeStrategy() {
        return new PollingJanitorStrategy(reader, subscriber, initializeJanitorScheduleHandler(), pollingConfiguration);
    }

    private Handler<EventsDTO> initializeJanitorScheduleHandler() {
        return new JanitorScheduleHandler(initializePipeline(), getErrorRouter());
    }

    private Pipeline<PipelineContext, PipelineContext> initializePipeline() {
        return Pipeline.<PipelineContext>start()
                .add(new ObservedStep<>("update-persisted", new UpdatePersistedEnvelopesStep(updater)))
                .add(new ObservedStep<>("quarantine-envelopes", new QuarantineFailedEnvelopesStep(updater)))
                .add(new ObservedStep<>("expired-envelopes",
                        new RemoveExpiredEnvelopesStep(Duration.ofMillis(millisecondsTtl), remover, reader)))
                .add(new ObservedStep<>("handle-stuck-envelopes",
                        new RepublishStuckEnvelopesStep(reader, publisher, Duration.ofMillis(millisecondsThreshold))));
    }
}
