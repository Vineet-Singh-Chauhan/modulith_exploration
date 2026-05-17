package com.wiredbarrack.modulith_exploration.notifications;

import com.wiredbarrack.modulith_exploration.notifications.internal.Notification;
import com.wiredbarrack.modulith_exploration.notifications.internal.NotificationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {
    private NotificationRepository notificationRepository;

    public Notification getNotification(Integer id){
        return notificationRepository.findById(id).orElseThrow(()->new RuntimeException("No notification found"));
    }
    public Notification saveNotification(String message){
        return notificationRepository.save(Notification.builder().message(message).status("PENDING").build());
    }
    public List<Notification> saveNotifications(List<Notification> notifications){
        return notificationRepository.saveAll(notifications);
    }
}
