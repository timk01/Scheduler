package taskplanner.scheduler.dto.planner;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.OffsetDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PlannerTaskResponse(
        Long taskId,
        String header,
        String text,
        TaskStatus status,
        OffsetDateTime finishedAt,
        Long ownerId
) {
}
