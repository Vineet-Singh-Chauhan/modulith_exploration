package com.wiredbarrack.modulith_exploration.notifications.internal;

import com.wiredbarrack.modulith_exploration.notifications.dto.Notification;
import com.wiredbarrack.modulith_exploration.notifications.NotificationService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
class NotificationServiceImpl implements NotificationService {
    private NotificationRepository notificationRepository;
    private NotificationMapper mapper;

    public Notification getNotification(Integer id){
        NotificationEntity notification = notificationRepository.findById(id).orElseThrow(()->new RuntimeException("No notification found"));
        return mapper.toRecord(notification);
    }
    public Notification saveNotification(String message){
        NotificationEntity notificationEntity = notificationRepository.save(NotificationEntity.builder().message(message).status("PENDING").build());
        return mapper.toRecord(notificationEntity);
    }
    public List<Notification> saveNotifications(List<Notification> notifications){
        List<NotificationEntity> notificationEntities = notifications.stream().map(mapper::toEntity).toList();
        notificationEntities = notificationRepository.saveAll(notificationEntities);
        return notificationEntities.stream().map(mapper::toRecord).toList();
    }
}
