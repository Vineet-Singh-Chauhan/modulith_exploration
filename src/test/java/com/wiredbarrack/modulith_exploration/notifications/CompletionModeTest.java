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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@Slf4j
class CompletionModeTest {


    @SpringBootTest
    static class UpdateModeTest {

        @Autowired ApplicationEventPublisher eventPublisher;
        @Autowired TransactionTemplate transactionTemplate;
        @Autowired JdbcTemplate jdbcTemplate;
        @MockitoBean NotificationService notificationService;

        @BeforeEach
        void setUp() {
            jdbcTemplate.execute("DELETE FROM event_publication");
            reset(notificationService);
        }

        @Test
        void updateMode_completedRowStaysInTable() throws InterruptedException {
            when(notificationService.saveNotification(anyString()))
                    .thenReturn(new Notification(1, "OK", "SENT"));

            transactionTemplate.executeWithoutResult(status ->
                    eventPublisher.publishEvent(OrderPlaced.builder().id(1).build()));

            Thread.sleep(2000);

            List<Map<String, Object>> publications =
                    jdbcTemplate.queryForList("SELECT * FROM event_publication");

            log.info("[UPDATE MODE] Rows in event_publication after success: {}", publications.size());
            if (!publications.isEmpty()) {
                log.info("  status          : {}", publications.get(0).get("status"));
                log.info("  completion_date : {}", publications.get(0).get("completion_date"));
            }

            // With UPDATE mode: row stays, completion_date is set, status is COMPLETED
            assertFalse(publications.isEmpty(),
                    "[UPDATE] Row must remain in the table — completed events are kept for auditing");
            assertEquals("COMPLETED", publications.get(0).get("status"),
                    "[UPDATE] status must be COMPLETED");
            assertNotNull(publications.get(0).get("completion_date"),
                    "[UPDATE] completion_date must be set");
        }
    }


    @SpringBootTest
    @TestPropertySource(properties = {
            "spring.modulith.events.completion-mode=DELETE"
    })
    static class DeleteModeTest {

        @Autowired ApplicationEventPublisher eventPublisher;
        @Autowired TransactionTemplate transactionTemplate;
        @Autowired JdbcTemplate jdbcTemplate;
        @MockitoBean NotificationService notificationService;

        @BeforeEach
        void setUp() {
            jdbcTemplate.execute("DELETE FROM event_publication");
            reset(notificationService);
        }

        @Test
        void deleteMode_rowIsRemovedAfterSuccessfulDelivery() throws InterruptedException {
            when(notificationService.saveNotification(anyString()))
                    .thenReturn(new Notification(1, "OK", "SENT"));

            transactionTemplate.executeWithoutResult(status ->
                    eventPublisher.publishEvent(OrderPlaced.builder().id(2).build()));

            Thread.sleep(2000);

            List<Map<String, Object>> publications =
                    jdbcTemplate.queryForList("SELECT * FROM event_publication");

            log.info("[DELETE MODE] Rows in event_publication after success: {}", publications.size());

            assertTrue(publications.isEmpty(),
                    "[DELETE] Row must be removed — DELETE mode deletes completed publications immediately");
        }

        @Test
        void deleteMode_failedRowStaysInTable() throws InterruptedException {
            when(notificationService.saveNotification(anyString()))
                    .thenThrow(new RuntimeException("Simulated failure"));

            transactionTemplate.executeWithoutResult(status ->
                    eventPublisher.publishEvent(OrderPlaced.builder().id(3).build()));

            Thread.sleep(2000);

            List<Map<String, Object>> publications =
                    jdbcTemplate.queryForList("SELECT * FROM event_publication");

            log.info("[DELETE MODE] Rows in event_publication after failure: {}", publications.size());
            if (!publications.isEmpty()) {
                log.info("  status: {}", publications.get(0).get("status"));
            }

            assertFalse(publications.isEmpty(),
                    "[DELETE] FAILED rows must NOT be deleted — they need to be resubmitted");
            assertEquals("FAILED", publications.get(0).get("status"),
                    "[DELETE] Failed publication must still be FAILED");
        }
    }
}
