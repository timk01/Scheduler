package taskplanner.scheduler.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import taskplanner.scheduler.TimeRestrictions;
import taskplanner.scheduler.dto.planner.UserTask;
import taskplanner.scheduler.dto.report.UserReport;
import taskplanner.scheduler.dto.summarization.request.SummarizationRequest;
import taskplanner.scheduler.dto.summarization.response.SummarizationResponse;
import taskplanner.scheduler.mapper.TaskMapper;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class ReportService {

    private final RestClient restClient;
    private final KafkaService kafkaService;
    private final TaskMapper taskMapper;

    @Value("${scheduler.auth-header}")
    private String header;

    @Value("${scheduler.auth-key}")
    private String key;

    public List<UserReport> getUserReports(TimeRestrictions timeRestrictions) {
        List<UserTask> userTasks = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/tasks/getScheduledTasks")
                        .queryParam("from", timeRestrictions.from())
                        .queryParam("to", timeRestrictions.to())
                        .build()
                )
                .header( header, key)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });

        List<UserReport> reports = new ArrayList<>();
        Instant from = timeRestrictions.from();
        Instant to = timeRestrictions.to();
        for (UserTask userTask : userTasks) {
            SummarizationRequest request = taskMapper.toSummarizationRequest(userTask, from, to);

            SummarizationResponse response = kafkaService.processSummarization(request);

            reports.add(taskMapper.toUserReport(userTask, response));
        }

        return reports;
    }
}
