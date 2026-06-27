package com.wiredbarrack.modulith_exploration.inventory;

import com.wiredbarrack.modulith_exploration.inventory.dto.Inventory;
import org.springframework.stereotype.Service;

@Service
public interface InventoryService {
    Inventory getInventory(Integer id);
    int getInventoryCount(Integer id);
    Inventory acquireInventoryItem(Integer id, int count);
    Inventory saveInventory(Inventory inventory);
}
