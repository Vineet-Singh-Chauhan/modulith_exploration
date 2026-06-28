package com.wiredbarrack.modulith_exploration.notifications;

import com.wiredbarrack.modulith_exploration.notifications.dto.Notification;
import com.wiredbarrack.modulith_exploration.orders.dto.OrderPlaced;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.modulith.events.FailedEventPublications;
import org.springframework.modulith.events.IncompleteEventPublications;
import org.springframework.modulith.events.ResubmissionOptions;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@Slf4j
@SpringBootTest
class ResubmissionApiTest {

    @Autowired ApplicationEventPublisher eventPublisher;
    @Autowired TransactionTemplate transactionTemplate;
    @Autowired JdbcTemplate jdbcTemplate;

    @Autowired FailedEventPublications failedEventPublications;
    @Autowired IncompleteEventPublications incompleteEventPublications;

    @MockitoBean NotificationService notificationService;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM event_publication");
        reset(notificationService);
    }

    @Test
    void failedPublication_canBeResubmittedAndCompleted() throws InterruptedException {

        when(notificationService.saveNotification(anyString()))
                .thenThrow(new RuntimeException("Simulated failure on first attempt"));

        transactionTemplate.executeWithoutResult(status ->
                eventPublisher.publishEvent(OrderPlaced.builder().id(42).build()));

        Thread.sleep(2000);

        List<Map<String, Object>> publications =
                jdbcTemplate.queryForList("SELECT * FROM event_publication");

        assertFalse(publications.isEmpty(), "Event should be stored after publishing");
        Map<String, Object> failedRow = publications.get(0);

        assertEquals("FAILED", failedRow.get("status"),
                "status should be FAILED after listener throws");

        log.info("Step 1 confirmed: status=FAILED, completion_attempts={}",
                failedRow.get("completion_attempts"));

        reset(notificationService);
        when(notificationService.saveNotification(anyString()))
                .thenReturn(new Notification(99, "Order placed!", "SENT"));

        failedEventPublications.resubmit(
                ResubmissionOptions.defaults()
                        .withBatchSize(10)
                        .withMinAge(Duration.ZERO) // no minimum age — pick up immediately
        );

        Thread.sleep(2000); // wait for async listener to run and succeed

        publications = jdbcTemplate.queryForList("SELECT * FROM event_publication");

        assertFalse(publications.isEmpty(), "Row should still exist in UPDATE completion mode");
        Map<String, Object> completedRow = publications.get(0);

        log.info("After resubmission:");
        log.info("  status                 : {}", completedRow.get("status"));
        log.info("  completion_attempts    : {}", completedRow.get("completion_attempts"));
        log.info("  last_resubmission_date : {}", completedRow.get("last_resubmission_date"));
        log.info("  completion_date        : {}", completedRow.get("completion_date"));

        assertEquals("COMPLETED", completedRow.get("status"),
                "status should be COMPLETED after successful resubmission");
        assertNotNull(completedRow.get("completion_date"),
                "completion_date should be set after successful delivery");
        assertNotNull(completedRow.get("last_resubmission_date"),
                "last_resubmission_date should be set when a resubmission occurred");
    }


    @Test
    void failedPublication_canBeResubmittedViaIncompletePublications() throws InterruptedException {

        when(notificationService.saveNotification(anyString()))
                .thenThrow(new RuntimeException("First attempt failure"));

        transactionTemplate.executeWithoutResult(status ->
                eventPublisher.publishEvent(OrderPlaced.builder().id(99).build()));

        Thread.sleep(2000);

        String status = (String) jdbcTemplate
                .queryForList("SELECT status FROM event_publication")
                .get(0).get("status");
        assertEquals("FAILED", status);
        log.info("Publication is FAILED — will use IncompleteEventPublications to resubmit");

        reset(notificationService);
        when(notificationService.saveNotification(anyString()))
                .thenReturn(new Notification(99, "OK", "SENT"));

        incompleteEventPublications.resubmitIncompletePublications(
                ResubmissionOptions.defaults()
                        .withMinAge(Duration.ZERO)
        );

        Thread.sleep(2000);

        String finalStatus = (String) jdbcTemplate
                .queryForList("SELECT status FROM event_publication")
                .get(0).get("status");
        assertEquals("COMPLETED", finalStatus,
                "IncompleteEventPublications should also be able to resubmit FAILED publications");
        log.info("Resubmission via IncompleteEventPublications confirmed: status=COMPLETED");
    }
}
