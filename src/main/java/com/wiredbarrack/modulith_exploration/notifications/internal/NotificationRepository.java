package com.wiredbarrack.modulith_exploration.notifications.internal;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
interface NotificationRepository extends ListCrudRepository<NotificationEntity,Integer> {
}
