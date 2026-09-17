package taskplanner.scheduler;

import java.time.Instant;

public record TimeRestrictions(
        Instant from,
        Instant to
) { }
