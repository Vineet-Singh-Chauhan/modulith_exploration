package com.wiredbarrack.modulith_exploration.inventory.internal;

import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@AllArgsConstructor
class InventoryDataSeeder implements CommandLineRunner {
    private InventoryRepository inventoryRepository;
    @Override
    public void run(String ...args){
        inventoryRepository.saveAll(List.of(Inventory.builder().name("ice cream").category("SWEETS").count(4).build(), Inventory.builder().name("tea").category("BEVERAGES").count(45).build()));
    }
}
