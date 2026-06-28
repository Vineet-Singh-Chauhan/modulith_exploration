package com.wiredbarrack.modulith_exploration.orders;

import com.wiredbarrack.modulith_exploration.inventory.InventoryService;
import com.wiredbarrack.modulith_exploration.inventory.dto.Inventory;
import com.wiredbarrack.modulith_exploration.orders.dto.Order;
import com.wiredbarrack.modulith_exploration.orders.dto.OrderPlaced;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.modulith.test.AssertablePublishedEvents;
import org.springframework.modulith.test.PublishedEvents;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@ApplicationModuleTest
class OrdersModuleTests {

    @Autowired
    OrderSevice orderService;

    @MockitoBean
    InventoryService inventoryService;

    @Test
    void placeOrder_publishesOrderPlacedEvent(PublishedEvents events) {

        when(inventoryService.getInventoryCount(null)).thenReturn(10);
        when(inventoryService.acquireInventoryItem(null, 2))
                .thenReturn(Inventory.builder().id(1).name("Widget").category("Tools").count(8).build());

        Order placed = orderService.placeOrder(new Order(null, 1, 2, "PENDING"));

        var published = events.ofType(OrderPlaced.class);
        assertThat(published)
                .as("placeOrder() must publish exactly one OrderPlaced event")
                .hasSize(1);

        var withCorrectId = events.ofType(OrderPlaced.class)
                .matchingValue(OrderPlaced::getId, placed.id());

        assertThat(withCorrectId)
                .as("The published event must carry the saved order id: " + placed.id())
                .hasSize(1);
    }
    @Test
    void placeOrder_publishesOrderPlacedEvent_assertableStyle(AssertablePublishedEvents events) {
        when(inventoryService.getInventoryCount(null)).thenReturn(10);
        when(inventoryService.acquireInventoryItem(null, 2))
                .thenReturn(Inventory.builder().id(1).name("Widget").category("Tools").count(8).build());

        Order placed = orderService.placeOrder(new Order(null, 1, 2, "PENDING"));

        assertThat(events)
                .contains(OrderPlaced.class)
                .matching(OrderPlaced::getId, placed.id());
    }
}
