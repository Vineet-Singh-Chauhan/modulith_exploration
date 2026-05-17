package com.wiredbarrack.modulith_exploration.notifications.internal;

import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class NotificationDataSeeder implements CommandLineRunner {
    private NotificationRepository inventoryRepository;

    @Override
    public void run(String ...args){
        inventoryRepository.save(Notification.builder().message("Order is delivered").status("PENDING").build());
    }
}
