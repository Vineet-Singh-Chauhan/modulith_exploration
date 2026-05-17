package com.wiredbarrack.modulith_exploration.inventory;

import com.wiredbarrack.modulith_exploration.inventory.internal.Inventory;
import com.wiredbarrack.modulith_exploration.inventory.internal.InventoryRepository;
import org.springframework.stereotype.Service;

@Service
public class InventoryService {
    private InventoryRepository inventoryRepository;

    public Inventory getInventory(Integer id){
        return inventoryRepository.findById(id).orElseThrow(()->new RuntimeException("No inventory found with this id"));
    }
}
