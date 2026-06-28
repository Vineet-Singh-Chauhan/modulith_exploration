package com.wiredbarrack.modulith_exploration.orders.internal;

import com.wiredbarrack.modulith_exploration.inventory.InventoryService;
import com.wiredbarrack.modulith_exploration.inventory.dto.Inventory;
import com.wiredbarrack.modulith_exploration.orders.dto.Order;
import com.wiredbarrack.modulith_exploration.orders.OrderSevice;
import com.wiredbarrack.modulith_exploration.orders.dto.OrderPlaced;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
class OrderSeviceImpl implements OrderSevice {
    private final OrderRepository orderRepository;
    private final InventoryService inventoryService;
    private final OrderMapper mapper;
    private final ApplicationEventPublisher events;

    public Order getOrder(Integer id){
        OrderEntity orderEntity= orderRepository.findById(id).orElseThrow(()->new RuntimeException("No order found with this Id"));
        return mapper.toRecord(orderEntity);
    }

    public List<Order> getPendingOrders(LocalDateTime time) {
      List<OrderEntity> orderEntities = orderRepository.getUpdatedOrdersInLastDay("PENDING", time);
      return orderEntities.stream().map(mapper::toRecord).toList();
    }

    @Transactional
    public Order placeOrder(Order order){
        int availableItems = inventoryService.getInventoryCount(order.id());
        if(availableItems<order.itemCount()){
            throw new RuntimeException("We dont have enough stocks for requested item, please come later!");
        }
        Inventory inventory = inventoryService.acquireInventoryItem(order.id(), order.itemCount());
        log.info("Inventory updated for item id : {} with count : {}", inventory.id(), inventory.count());
        OrderEntity orderEntity = mapper.toEntity(order);
        orderEntity.setStatus("PENDING");
        orderEntity = orderRepository.save(orderEntity);
        log.info("Order Placed with id : {}", orderEntity.getId());
        events.publishEvent(OrderPlaced.builder().id(orderEntity.getId()).build());
        return mapper.toRecord(orderEntity);
    }
}
