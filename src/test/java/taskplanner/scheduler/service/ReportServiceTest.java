package taskplanner.scheduler.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;
import taskplanner.scheduler.TimeRestrictions;
import taskplanner.scheduler.dto.planner.PlannerTaskResponse;
import taskplanner.scheduler.dto.planner.TaskStatus;
import taskplanner.scheduler.dto.planner.UserTask;
import taskplanner.scheduler.dto.report.UserReport;
import taskplanner.scheduler.dto.summarization.request.SummarizationRequest;
import taskplanner.scheduler.dto.summarization.request.TaskRequest;
import taskplanner.scheduler.dto.summarization.request.TaskStatusRequestEnum;
import taskplanner.scheduler.dto.summarization.response.SummarizationResponse;
import taskplanner.scheduler.mapper.TaskMapper;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @InjectMocks
    private ReportService reportService;

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.RequestHeadersUriSpec requestSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    @Mock
    private KafkaService kafkaService;

    @Mock
    private TaskMapper taskMapper;


    @Test
    public void getUserReportIsSucceeded() {
        String header = "X-Scheduler-Auth";
        String key = "test-scheduler-token";

        ReflectionTestUtils.setField(reportService, "header", header);
        ReflectionTestUtils.setField(reportService, "key", key);

        when(restClient.get()).thenReturn(requestSpec);

        when(requestSpec.uri(any(Function.class)))
                .thenReturn(requestSpec);

        when(requestSpec.header(header, key))
                .thenReturn(requestSpec);

        when(requestSpec.retrieve())
                .thenReturn(responseSpec);

        String email = "user@mail.ru";
        List<UserTask> userTasks = List.of(
                new UserTask(
                        1L,
                        email,
                        List.of(
                                new PlannerTaskResponse(
                                        10L,
                                        "Finished task",
                                        "Finished task text",
                                        TaskStatus.FINISHED,
                                        OffsetDateTime.parse("2026-09-24T20:30:00+03:00"),
                                        1L
                                )
                        ),
                        List.of(
                                new PlannerTaskResponse(
                                        11L,
                                        "Unfinished task",
                                        "Unfinished task text",
                                        TaskStatus.CREATED,
                                        null,
                                        1L
                                )
                        )
                )
        );

        when(responseSpec.body(any(ParameterizedTypeReference.class)))
                .thenReturn(userTasks);


        TimeRestrictions timeRestrictions = new TimeRestrictions(
                Instant.parse("2026-09-23T20:00:00Z"),
                Instant.parse("2026-09-24T20:00:00Z")
        );

        Instant from = timeRestrictions.from();
        Instant to = timeRestrictions.to();

        UserTask task = userTasks.getFirst();
        SummarizationRequest request = new SummarizationRequest(
                Instant.parse("2026-09-23T20:00:00Z"),
                Instant.parse("2026-09-24T20:00:00Z"),
                List.of(
                        new TaskRequest(
                                "Finished task",
                                "Finished task text",
                                TaskStatusRequestEnum.FINISHED,
                                OffsetDateTime.parse("2026-09-24T20:30:00+03:00")
                        )
                ),
                List.of(
                        new TaskRequest(
                                "Unfinished task",
                                "Unfinished task text",
                                TaskStatusRequestEnum.CREATED,
                                null
                        )
                )
        );

        when(taskMapper.toSummarizationRequest(task, from, to)).thenReturn(request);

        SummarizationResponse response =
                new SummarizationResponse("Test summarization");

        when(kafkaService.processSummarization(request)).thenReturn(response);

        UserReport userReport = new UserReport(
                email,
                response.report()
        );

        when(taskMapper.toUserReport(task, response)).thenReturn(userReport);

        List<UserReport> actual = reportService.getUserReports(timeRestrictions);

        verify(taskMapper).toSummarizationRequest(
                task,
                timeRestrictions.from(),
                timeRestrictions.to()
        );

        verify(kafkaService).processSummarization(request);

        verify(taskMapper).toUserReport(task, response);

        UserReport expectedReport = new UserReport(
                "user@mail.ru",
                "Test summarization"
        );

        assertThat(actual).containsExactly(expectedReport);
    }
}
