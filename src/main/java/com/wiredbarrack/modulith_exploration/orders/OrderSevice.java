package com.wiredbarrack.modulith_exploration.orders;

import com.wiredbarrack.modulith_exploration.inventory.Inventory;
import com.wiredbarrack.modulith_exploration.inventory.InventoryRepository;
import com.wiredbarrack.modulith_exploration.notifications.Notification;
import com.wiredbarrack.modulith_exploration.notifications.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class OrderSevice {
    private OrderRepository orderRepository;
    private InventoryRepository inventoryRepository;
    private NotificationService notificationService;

    public Order getOrder(Integer id){
        return orderRepository.findById(id).orElseThrow(()->new RuntimeException("No order found with this Id"));
    }

    public List<Order> getPendingOrders(LocalDateTime time) {
        return orderRepository.getUpdatedOrdersInLastDay("PENDING", time);
    }

    public Order placeOrder(Order order){
        Inventory orderInvetoryDetails = inventoryRepository.findById(order.getId()).orElseThrow(()->new RuntimeException("No item with given id exists in inventory ?"));
        if(orderInvetoryDetails.getCount()<order.getItemCount()){
            throw new RuntimeException("We dont have enough stocks for requested item, please come later!");
        }
        orderInvetoryDetails.setCount(orderInvetoryDetails.getCount()-order.getItemCount());
        inventoryRepository.save(orderInvetoryDetails);
        order.setStatus("PENDING");
        Order orderPlaced = orderRepository.save(order);
        log.info("Order Placed with id : {}", orderPlaced.getId());
        notificationService.saveNotification(Notification.builder().message("Your Order is under processing!").build());
        return orderPlaced;
    }
}
