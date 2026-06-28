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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;


@Slf4j
@SpringBootTest
class DurableEventsTest {

    @Autowired
    ApplicationEventPublisher eventPublisher;

    @Autowired
    TransactionTemplate transactionTemplate;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @MockitoBean
    NotificationService notificationService;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM event_publication");
        reset(notificationService);
    }

    @Test
    void whenListenerFails_eventPublicationRemainsIncomplete() throws InterruptedException {

        when(notificationService.saveNotification(anyString()))
                .thenThrow(new RuntimeException("Simulated notification failure!"));

        transactionTemplate.executeWithoutResult(status ->
                eventPublisher.publishEvent(OrderPlaced.builder().id(1).build()));

        Thread.sleep(2000);

        List<Map<String, Object>> publications = jdbcTemplate.queryForList("SELECT * FROM event_publication");

        logPublicationTable(publications, "FAILURE SCENARIO");

        assertFalse(publications.isEmpty(),
                "event_publication must have at least one row — event was published");

        boolean hasIncompletePublication = publications.stream()
                .anyMatch(row -> row.get("completion_date") == null);

        assertTrue(hasIncompletePublication,
                "completion_date must be null — listener threw, event is pending re-delivery");

        log.info("FAILURE SCENARIO confirmed: event stored safely, completion_date = null, awaiting re-delivery.");
    }

    @Test
    void whenListenerSucceeds_eventPublicationIsMarkedComplete() throws InterruptedException {

        when(notificationService.saveNotification(anyString()))
                .thenReturn(new Notification(99, "Your order has been placed successfully!", "SENT"));

        transactionTemplate.executeWithoutResult(status ->
                eventPublisher.publishEvent(OrderPlaced.builder().id(10).build()));

        Thread.sleep(2000);

        List<Map<String, Object>> publications = jdbcTemplate.queryForList("SELECT * FROM event_publication");

        logPublicationTable(publications, "SUCCESS SCENARIO");

        assertFalse(publications.isEmpty(),
                "event_publication must have at least one row — event was published");

        boolean hasCompletedPublication = publications.stream()
                .anyMatch(row -> row.get("completion_date") != null);

        assertTrue(hasCompletedPublication,
                "completion_date must be set — listener succeeded, event delivery is complete");

        log.info("SUCCESS SCENARIO confirmed: event published, listener ran, completion_date is set.");
    }

    private void logPublicationTable(List<Map<String, Object>> publications, String scenario) {
        log.info("=== event_publication — {} ({} rows) ===", scenario, publications.size());
        for (Map<String, Object> row : publications) {
            log.info("  id              : {}", row.get("id"));
            log.info("  event_type      : {}", row.get("event_type"));
            log.info("  listener_id     : {}", row.get("listener_id"));
            log.info("  publication_date: {}", row.get("publication_date"));
            log.info("  completion_date : {}", row.get("completion_date")); // null = not delivered yet
            log.info("  serialized_event: {}", row.get("serialized_event"));
            log.info("  status          : {}", row.get("status"));
            log.info("  ----");
        }
    }
}

