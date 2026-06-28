package com.wiredbarrack.modulith_exploration.utility;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.CompletedEventPublications;
import org.springframework.modulith.events.FailedEventPublications;
import org.springframework.modulith.events.IncompleteEventPublications;
import org.springframework.modulith.events.ResubmissionOptions;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventHousekeeping {

    private final FailedEventPublications failedEventPublications;
    private final IncompleteEventPublications incompleteEventPublications;
    private final CompletedEventPublications completedEventPublications;

    @Scheduled(fixedDelay = 300_000) // every 5 minutes
    public void resubmitFailedPublications() {
        log.info("Housekeeping: resubmitting failed event publications...");

        failedEventPublications.resubmit(
                ResubmissionOptions.defaults()
                        .withBatchSize(50)
                        .withMinAge(Duration.ofMinutes(2))
                        .withFilter(pub -> pub.getCompletionAttempts() < 5) // stop after 5 tries
        );
    }

    @Scheduled(fixedDelay = 900_000) // every 15 minutes
    public void resubmitIncompletePublications() {
        log.info("Housekeeping: resubmitting incomplete event publications older than 10 minutes...");

        incompleteEventPublications.resubmitIncompletePublications(
                ResubmissionOptions.defaults()
                        .withMinAge(Duration.ofMinutes(10))
                        .withBatchSize(20)
        );
    }


    @Scheduled(fixedDelay = 1_800_000) // every 30 minutes
    public void purgeCompletedPublications() {
        log.info("Housekeeping: purging completed event publications older than 1 hour...");
        completedEventPublications.deletePublicationsOlderThan(Duration.ofHours(1));
    }
}
