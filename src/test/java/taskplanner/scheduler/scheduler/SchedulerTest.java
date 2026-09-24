package taskplanner.scheduler.scheduler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import taskplanner.scheduler.TimeRestrictions;
import taskplanner.scheduler.dto.report.UserReport;
import taskplanner.scheduler.service.KafkaService;
import taskplanner.scheduler.service.ReportService;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static taskplanner.scheduler.config.SchedulerMainConfig.TIME_ZONE;

@ExtendWith(MockitoExtension.class)
class SchedulerTest {

    @InjectMocks
    private Scheduler scheduler;

    @Mock
    private Clock clock;

    @Mock
    private ReportService reportService;

    @Mock
    private KafkaService kafkaService;

    @Test
    public void processingUserTasksIsSucceeded() {
        Instant now = Instant.parse("2026-09-24T12:00:00Z");

        when(clock.instant()).thenReturn(now);
        when(clock.getZone()).thenReturn(ZoneId.of(TIME_ZONE));

        TimeRestrictions timeRestrictions = new TimeRestrictions(
                Instant.parse("2026-09-23T20:00:00Z"),
                Instant.parse("2026-09-24T20:00:00Z")
        );

        List<UserReport> reports = List.of(
                new UserReport(
                        "user@mail.ru",
                        "Test summarization"
                )
        );

        when(reportService.getUserReports(timeRestrictions)).thenReturn(reports);

        scheduler.processUserTasks();

        verify(reportService).getUserReports(timeRestrictions);
        verify(kafkaService).sendMessage(reports.getFirst());
    }
}