package taskplanner.scheduler.dto.planner;

import java.util.List;

public record UserTasks(
        Long userId,
        String email,
        List<PlannerTaskResponse> finishedTasks,
        List<PlannerTaskResponse> unfinishedTasks
) {
}