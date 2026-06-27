package com.wiredbarrack.modulith_exploration.notifications;

import com.wiredbarrack.modulith_exploration.orders.dto.OrderPlaced;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.modulith.test.Scenario;

@ApplicationModuleTest(ApplicationModuleTest.BootstrapMode.DIRECT_DEPENDENCIES)
class NotificationsModuleTests {

    @Test
    void checkEventListenerForOrderNotification(Scenario scenario) {

        var event = OrderPlaced.builder().id(42).build();

        scenario.publish(event)
                .andWaitForStateChange(() -> event);
    }
}
