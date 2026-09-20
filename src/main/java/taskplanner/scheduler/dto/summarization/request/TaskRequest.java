package taskplanner.scheduler.dto.summarization.request;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.OffsetDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TaskRequest(
        String header,
        String text,
        TaskStatusRequestEnum status,
        OffsetDateTime finishedAt
) {
}
