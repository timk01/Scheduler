package taskplanner.scheduler.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import taskplanner.scheduler.TimeRestrictions;
import taskplanner.scheduler.dto.planner.PlannerTaskResponse;
import taskplanner.scheduler.dto.planner.TaskStatus;
import taskplanner.scheduler.dto.planner.UserTasks;
import taskplanner.scheduler.dto.report.UserReport;
import taskplanner.scheduler.dto.summarization.request.SummarizationRequest;
import taskplanner.scheduler.dto.summarization.request.TaskRequest;
import taskplanner.scheduler.dto.summarization.request.TaskStatusRequestEnum;
import taskplanner.scheduler.dto.summarization.response.SummarizationResponse;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ReportService {

    private final RestClient restClient;
    private final KafkaService kafkaService;

    public List<UserReport> getUserReports(TimeRestrictions timeRestrictions) {
/*        List<UserTasks> userTasks = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/tasks/getScheduledTasks")
                        .queryParam("from", timeRestrictions.from())
                        .queryParam("to", timeRestrictions.to())
                        .build()
                )
                .retrieve()
                .body(new ParameterizedTypeReference<List<UserTasks>>() {
                });*/

        List<UserTasks> userTasks = List.of(
                new UserTasks(
                        1L,
                        "user1@test.com",
                        List.of(
                                new PlannerTaskResponse(
                                        101L,
                                        "Изучить GigaChat API",
                                        "Разобраться с OAuth и получить access token",
                                        TaskStatus.FINISHED,
                                        OffsetDateTime.parse("2026-09-20T10:30:00Z"),
                                        1L
                                ),
                                new PlannerTaskResponse(
                                        102L,
                                        "Проверить первый summary",
                                        "Отправить тестовые данные и проверить ответ модели",
                                        TaskStatus.FINISHED,
                                        OffsetDateTime.parse("2026-09-20T14:15:00Z"),
                                        1L
                                )
                        ),
                        List.of(
                                new PlannerTaskResponse(
                                        103L,
                                        "Настроить Kafka RPC",
                                        "Сделать request/reply между Scheduler и Summarization",
                                        TaskStatus.IN_PROCESS,
                                        null,
                                        1L
                                ),
                                new PlannerTaskResponse(
                                        104L,
                                        "Добавить MapStruct",
                                        "Перенести ручной mapping DTO в mapper",
                                        TaskStatus.CREATED,
                                        null,
                                        1L
                                )
                        )
                ),

                new UserTasks(
                        2L,
                        "user2@test.com",
                        List.of(
                                new PlannerTaskResponse(
                                        201L,
                                        "Исправить отчёт",
                                        "Подготовить итоговый отчёт пользователя",
                                        TaskStatus.FINISHED,
                                        OffsetDateTime.parse("2026-09-20T18:40:00Z"),
                                        2L
                                )
                        ),
                        List.of(
                                new PlannerTaskResponse(
                                        202L,
                                        "Настроить Email Sender",
                                        "Передать готовый отчёт через Kafka",
                                        TaskStatus.CREATED,
                                        null,
                                        2L
                                ),
                                new PlannerTaskResponse(
                                        203L,
                                        "Написать тесты Scheduler",
                                        "Проверить интеграцию Scheduler и Summarization",
                                        TaskStatus.IN_PROCESS,
                                        null,
                                        2L
                                )
                        )
                )
        );

        List<UserReport> reports = new ArrayList<>();
        for (UserTasks userTask : userTasks) { //toDo assert ?
            List<TaskRequest> finishedTasks = userTask.finishedTasks().stream()
                    .map(
                            task -> new TaskRequest(
                                    task.header(),
                                    task.text(),
                                    TaskStatusRequestEnum.valueOf(task.status().name()),
                                    task.finishedAt()
                            )
                    )
                    .toList();

            List<TaskRequest> unfinishedTasks = userTask.unfinishedTasks().stream()
                    .map(
                            task -> new TaskRequest(
                                    task.header(),
                                    task.text(),
                                    TaskStatusRequestEnum.valueOf(task.status().name()),
                                    task.finishedAt()
                            )
                    )
                    .toList();

            SummarizationResponse summarizationResponse = kafkaService.processSummarization(
                    new SummarizationRequest(
                            timeRestrictions.from(),
                            timeRestrictions.to(),
                            finishedTasks,
                            unfinishedTasks
                    )
            );
            UserReport userReport = new UserReport(userTask.email(), summarizationResponse.report());

            reports.add(userReport);
        }

        return reports;
    }
}
