package com.wiredbarrack.modulith_exploration.orders.internal;

import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;


@Component
@AllArgsConstructor
class OrderDataSeeder implements CommandLineRunner {
    private OrderRepository orderRepository;

    @Override
    public void run(String ...args){
        orderRepository.save(OrderEntity.builder().itemId(1).status("DELIVERED").build());
    }
}
