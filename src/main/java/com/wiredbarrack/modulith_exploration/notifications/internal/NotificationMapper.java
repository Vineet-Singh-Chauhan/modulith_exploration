package com.wiredbarrack.modulith_exploration.notifications.internal;

import com.wiredbarrack.modulith_exploration.notifications.dto.Notification;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
interface NotificationMapper {
    Notification toRecord(NotificationEntity entity);
    NotificationEntity toEntity(Notification record);
}
