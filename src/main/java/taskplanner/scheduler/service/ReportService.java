package taskplanner.scheduler.service;

import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import taskplanner.scheduler.TimeRestrictions;
import taskplanner.scheduler.dto.UserTasks;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ReportService {

    private final RestClient restClient;

    public List<UserTasks> getReportDetails(TimeRestrictions timeRestrictions) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/tasks/getScheduledTasks")
                        .queryParam("from", timeRestrictions.from())
                        .queryParam("to", timeRestrictions.to())
                        .build()
                )
                .retrieve()
                .body(new ParameterizedTypeReference<List<UserTasks>>() {});
    }
}
