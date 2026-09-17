package taskplanner.scheduler.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import taskplanner.scheduler.service.ReportService;
import taskplanner.scheduler.TimeRestrictions;

import java.time.Clock;
import java.time.ZonedDateTime;

import static taskplanner.scheduler.config.SchedulerMainConfig.TIME_ZONE;

@RequiredArgsConstructor
@Service
public class Scheduler {

    private static final int REPORT_HOUR = 23;
    private static final String PREFERRED_SCHEDULE = "0 0 " + REPORT_HOUR + " * * ?";

    private final Clock clock;

    private final ReportService service;

    @Scheduled(cron = PREFERRED_SCHEDULE, zone = TIME_ZONE)
    public void getUserTasks() {
        TimeRestrictions timeRestrictions = calculateTimeRestrictions();

        service.getReportDetails(timeRestrictions); //??? ппока не ясно
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

