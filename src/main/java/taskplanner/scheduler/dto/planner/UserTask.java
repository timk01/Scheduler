package taskplanner.scheduler.dto.planner;

import java.util.List;

public record UserTask(
        Long userId,
        String email,
        List<PlannerTaskResponse> finishedTasks,
        List<PlannerTaskResponse> unfinishedTasks
) {
}