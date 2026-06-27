package com.wiredbarrack.modulith_exploration.orders;

import com.wiredbarrack.modulith_exploration.orders.dto.Order;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public interface OrderSevice {
    Order getOrder(Integer id);
    List<Order> getPendingOrders(LocalDateTime time);
    Order placeOrder(Order order);
}
