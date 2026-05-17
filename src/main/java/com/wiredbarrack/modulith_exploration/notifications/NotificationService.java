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
    public Notification saveNotification(Notification notification){
        notification.setStatus("PENDING");
        return notificationRepository.save(notification);
    }
    public List<Notification> saveNotifications(List<Notification> notifications){
        return notificationRepository.saveAll(notifications);
    }
}
