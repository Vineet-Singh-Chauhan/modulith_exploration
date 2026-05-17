package com.wiredbarrack.modulith_exploration.orders;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends ListCrudRepository<Order,Integer> {
    List<Order> findByStatus(String status);
    @Query("SELECT o FROM Order o WHERE o.status = :status AND o.updatedAt <= :time")
    List<Order> getUpdatedOrdersInLastDay(String status, LocalDateTime time);

}
