package software.spool.janitor.internal.control;

import software.spool.core.model.event.EnvelopePersisted;
import software.spool.core.model.failure.EnvelopeQuarantined;

import java.util.Collection;

public record EventsDTO(
        Collection<EnvelopePersisted> envelopesPersisted,
        Collection<EnvelopeQuarantined> envelopesQuarantined
) {
}
