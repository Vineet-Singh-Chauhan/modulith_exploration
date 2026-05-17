package com.wiredbarrack.modulith_exploration.notifications;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends ListCrudRepository<Notification,Integer> {
}
