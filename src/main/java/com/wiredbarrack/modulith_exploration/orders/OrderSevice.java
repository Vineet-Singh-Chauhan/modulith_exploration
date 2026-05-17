package com.wiredbarrack.modulith_exploration.orders;

import com.wiredbarrack.modulith_exploration.inventory.InventoryService;
import com.wiredbarrack.modulith_exploration.notifications.NotificationService;
import com.wiredbarrack.modulith_exploration.orders.internal.Order;
import com.wiredbarrack.modulith_exploration.orders.internal.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class OrderSevice {
    private OrderRepository orderRepository;
    private InventoryService inventoryService;
    private NotificationService notificationService;

    public Order getOrder(Integer id){
        return orderRepository.findById(id).orElseThrow(()->new RuntimeException("No order found with this Id"));
    }

    public List<Order> getPendingOrders(LocalDateTime time) {
        return orderRepository.getUpdatedOrdersInLastDay("PENDING", time);
    }

    public Order placeOrder(Order order){
        int availableItems = inventoryService.getInventoryCount(order.getId());
        if(availableItems<order.getItemCount()){
            throw new RuntimeException("We dont have enough stocks for requested item, please come later!");
        }
        inventoryService.acquireInventoryItem(order.getId(), order.getItemCount());
        order.setStatus("PENDING");
        Order orderPlaced = orderRepository.save(order);
        log.info("Order Placed with id : {}", orderPlaced.getId());
        notificationService.saveNotification("Your Order is under processing!");
        return orderPlaced;
    }
}
