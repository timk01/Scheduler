package taskplanner.scheduler.dto;

import java.util.List;

public record UserTasks(
        Long userId,
        String email,
        List<TaskResponse> finishedTasks,
        List<TaskResponse> unfinishedTasks
) {
}