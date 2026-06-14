package com.wiredbarrack.modulith_exploration.orders.internal;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
interface OrderRepository extends ListCrudRepository<OrderEntity,Integer> {
    List<OrderEntity> findByStatus(String status);
    @Query("SELECT o FROM OrderEntity o WHERE o.status = :status AND o.updatedAt <= :time")
    List<OrderEntity> getUpdatedOrdersInLastDay(String status, LocalDateTime time);

}
