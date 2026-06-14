package com.wiredbarrack.modulith_exploration.inventory.internal;

import com.wiredbarrack.modulith_exploration.inventory.InventoryService;
import org.springframework.stereotype.Service;

@Service
class InventoryServiceImpl implements InventoryService {
    private InventoryRepository inventoryRepository;

    public Inventory getInventory(Integer id){
        return inventoryRepository.findById(id).orElseThrow(()->new RuntimeException("No inventory found with this id"));
    }

    public int getInventoryCount(Integer id){
         Inventory inventory = inventoryRepository.findById(id).orElseThrow(()->new RuntimeException("No inventory found with this id"));
         return inventory.getCount();
    }

    public Inventory acquireInventoryItem(Integer id, int count) {
        Inventory inventory = inventoryRepository.findById(id).orElseThrow(()->new RuntimeException("No inventory found with this id"));
        if(inventory.getCount()<count){
          throw  new RuntimeException("Not enough items!");
        }
        inventory.setCount(inventory.getCount()-count);
       return inventoryRepository.save(inventory);
    }

    public Inventory saveInventory(Inventory inventory){
        return inventoryRepository.save(inventory);
    }
}
