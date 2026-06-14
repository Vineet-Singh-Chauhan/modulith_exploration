package com.wiredbarrack.modulith_exploration.notifications.internal;

import com.wiredbarrack.modulith_exploration.notifications.NotificationService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
class NotificationServiceImpl implements NotificationService {
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
