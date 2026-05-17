package com.wiredbarrack.modulith_exploration.orders;

import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;


@Component
@AllArgsConstructor
public class OrderDataSeeder implements CommandLineRunner {
    private OrderRepository orderRepository;

    @Override
    public void run(String ...args){
        orderRepository.save(Order.builder().itemId(1).status("DELIVERED").build());
    }
}
