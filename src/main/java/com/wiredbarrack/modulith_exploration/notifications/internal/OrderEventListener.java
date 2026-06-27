package com.wiredbarrack.modulith_exploration.notifications.internal;

import com.wiredbarrack.modulith_exploration.notifications.NotificationService;
import com.wiredbarrack.modulith_exploration.orders.dto.OrderPlaced;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
class OrderEventListener {

    private final NotificationService notificationService;

    @ApplicationModuleListener
    void onOrderPlaced(OrderPlaced event) {
        log.info("Received OrderPlaced event for order id: {}", event.getId());
        notificationService.saveNotification("Your order has been placed successfully!");
    }
}
