package taskplanner.scheduler.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import taskplanner.scheduler.dto.report.UserReport;
import taskplanner.scheduler.service.ReportService;
import taskplanner.scheduler.TimeRestrictions;
import tools.jackson.databind.ObjectMapper;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.List;

import static taskplanner.scheduler.config.SchedulerMainConfig.TIME_ZONE;

@Slf4j
@RequiredArgsConstructor
@Service
public class Scheduler {

    private static final int REPORT_HOUR = 23;
    private static final String PREFERRED_SCHEDULE = "0 46 15 * * ?";//"0 0 " + REPORT_HOUR + " * * ?";

    private final Clock clock;

    private final ReportService service;

    @Scheduled(cron = PREFERRED_SCHEDULE, zone = TIME_ZONE)
    public void getUserTasks() {
        TimeRestrictions timeRestrictions = calculateTimeRestrictions();

        List<UserReport> userReports = service.getUserReports(timeRestrictions);

        ObjectMapper objectMapper = new ObjectMapper();
        log.info(
                "Scheduled report data: {}",
                objectMapper.writeValueAsString(userReports)
        );
    }

    @EventListener(ApplicationReadyEvent.class)
    public void runOnce() throws InterruptedException {
        getUserTasks();
    }

    private TimeRestrictions calculateTimeRestrictions() {
        ZonedDateTime now = ZonedDateTime.now(clock);

        ZonedDateTime to = now.withHour(REPORT_HOUR)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);

        ZonedDateTime from = to.minusDays(1);

        return new TimeRestrictions(
                from.toInstant(),
                to.toInstant()
        );
    }
}

