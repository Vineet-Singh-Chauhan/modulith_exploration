package com.wiredbarrack.modulith_exploration.orders.internal;

import com.wiredbarrack.modulith_exploration.orders.dto.OrderPlaced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.modulith.events.EventExternalizationConfiguration;

import java.time.Instant;


@Configuration
class EventExternalizationConfig {


    @Bean
    EventExternalizationConfiguration orderEventExternalizationConfiguration() {
        return EventExternalizationConfiguration.externalizing()                          // (1)
                .select(EventExternalizationConfiguration.annotatedAsExternalized())      // (2)
                .mapping(OrderPlaced.class, event ->                                      // (3)
                        new ExternalOrderEvent(event.getId(), Instant.now())
                )
                .routeKey(OrderPlaced.class, e -> String.valueOf(e.getId()))              // (4)
                .build();
    }

    record ExternalOrderEvent(Integer orderId, Instant occurredAt) {}
}
