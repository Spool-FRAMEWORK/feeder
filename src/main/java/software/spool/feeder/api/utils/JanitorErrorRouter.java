package software.spool.feeder.api.utils;

import software.spool.core.adapter.logging.LoggerFactory;
import software.spool.core.exception.*;
import software.spool.core.port.bus.EventPublisher;
import software.spool.core.utils.routing.ErrorRouter;

public class JanitorErrorRouter {
        public static ErrorRouter defaults(EventPublisher publisher) {
                return new ErrorRouter()
                        .on(EventBrokerEmitException.class, (e, cause) ->
                                LoggerFactory.getLogger(EventBrokerEmitException.class).error(e.getMessage()));
        }
}
