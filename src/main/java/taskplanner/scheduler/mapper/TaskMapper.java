package taskplanner.scheduler.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import taskplanner.scheduler.dto.planner.PlannerTaskResponse;
import taskplanner.scheduler.dto.planner.TaskStatus;
import taskplanner.scheduler.dto.planner.UserTask;
import taskplanner.scheduler.dto.report.UserReport;
import taskplanner.scheduler.dto.summarization.request.SummarizationRequest;
import taskplanner.scheduler.dto.summarization.request.TaskRequest;
import taskplanner.scheduler.dto.summarization.request.TaskStatusRequestEnum;
import taskplanner.scheduler.dto.summarization.response.SummarizationResponse;

import java.time.Instant;
import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TaskMapper {

    TaskStatusRequestEnum toTaskStatusRequestEnum(TaskStatus taskStatus);

    TaskRequest toTaskRequest(PlannerTaskResponse plannerTaskResponse);

    List<TaskRequest> toTaskRequestList(List<PlannerTaskResponse> plannerTaskResponses);

    @Mapping(source = "from", target = "from")
    @Mapping(source = "to", target = "to")
    SummarizationRequest toSummarizationRequest(UserTask userTask, Instant from, Instant to);

    @Mapping(source = "userTask.email", target = "email")
    @Mapping(source = "response.report", target = "summarization")
    UserReport toUserReport(UserTask userTask, SummarizationResponse response);
}
