package com.wiredbarrack.modulith_exploration.orders.internal;

import com.wiredbarrack.modulith_exploration.inventory.InventoryService;
import com.wiredbarrack.modulith_exploration.notifications.NotificationService;
import com.wiredbarrack.modulith_exploration.orders.dto.Order;
import com.wiredbarrack.modulith_exploration.orders.OrderSevice;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
class OrderSeviceImpl implements OrderSevice {
    private OrderRepository orderRepository;
    private InventoryService inventoryService;
    private NotificationService notificationService;
    private OrderMapper mapper;

    public Order getOrder(Integer id){
        OrderEntity orderEntity= orderRepository.findById(id).orElseThrow(()->new RuntimeException("No order found with this Id"));
        return mapper.toRecord(orderEntity);
    }

    public List<Order> getPendingOrders(LocalDateTime time) {
      List<OrderEntity> orderEntities = orderRepository.getUpdatedOrdersInLastDay("PENDING", time);
      return orderEntities.stream().map(mapper::toRecord).toList();
    }

    public Order placeOrder(Order order){
        int availableItems = inventoryService.getInventoryCount(order.id());
        if(availableItems<order.itemCount()){
            throw new RuntimeException("We dont have enough stocks for requested item, please come later!");
        }
        inventoryService.acquireInventoryItem(order.id(), order.itemCount());
        OrderEntity orderEntity = mapper.toEntity(order);
        orderEntity.setStatus("PENDING");
        orderEntity = orderRepository.save(orderEntity);
        log.info("Order Placed with id : {}", orderEntity.getId());
        notificationService.saveNotification("Your Order is under processing!");
        return mapper.toRecord(orderEntity);
    }
}
