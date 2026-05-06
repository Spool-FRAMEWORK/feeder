package software.spool.janitor.internal.control;

import software.spool.core.exception.SpoolException;
import software.spool.core.pipeline.Pipeline;
import software.spool.core.pipeline.PipelineContext;
import software.spool.core.port.bus.Handler;
import software.spool.core.utils.routing.ErrorRouter;

public class JanitorScheduleHandler implements Handler<EventsDTO
        > {
    private final Pipeline<PipelineContext, PipelineContext> pipeline;
    private final ErrorRouter errorRouter;

    public JanitorScheduleHandler(Pipeline<PipelineContext, PipelineContext> pipeline, ErrorRouter errorRouter) {
        this.pipeline = pipeline;
        this.errorRouter = errorRouter;
    }

    @Override
    public void handle(EventsDTO events) throws SpoolException {
        try {
            pipeline.execute(PipelineContext.empty()
                    .with(JanitorScheduleKeys.ENVELOPES_PERSISTED, events.envelopesPersisted())
                    .with(JanitorScheduleKeys.ENVELOPES_QUARANTINED, events.envelopesQuarantined()));
        } catch (Exception e) {
            errorRouter.dispatch(e);
        }
    }
}
