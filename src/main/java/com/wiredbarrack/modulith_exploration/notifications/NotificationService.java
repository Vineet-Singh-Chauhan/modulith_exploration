package com.wiredbarrack.modulith_exploration.notifications;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface NotificationService {
    Notification getNotification(Integer id);
    Notification saveNotification(String message);
    List<Notification> saveNotifications(List<Notification> notifications);
}
