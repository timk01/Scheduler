package taskplanner.scheduler.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class SchedulerMainConfig {

    public static final String TIME_ZONE = "Europe/Moscow";

    @Bean
    public Clock clock() {
        return Clock.system(ZoneId.of(TIME_ZONE));
    }

    @Bean
    public RestClient restClient(
            @Value("${task-planner.base-url}") String taskPlannerBaseUrl
    ) {
        return RestClient.builder()
                .baseUrl(taskPlannerBaseUrl)
                .build();
    }
}
