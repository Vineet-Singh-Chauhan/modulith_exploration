package com.wiredbarrack.modulith_exploration.inventory.internal;

import com.wiredbarrack.modulith_exploration.inventory.InventoryService;
import com.wiredbarrack.modulith_exploration.inventory.dto.Inventory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
class InventoryServiceImpl implements InventoryService {
    private final InventoryRepository inventoryRepository;
    private final InventoryMapper mapper;

    public Inventory getInventory(Integer id) {
        InventoryEntity inventoryEntity = inventoryRepository.findById(id).orElseThrow(() -> new RuntimeException("No inventory found with this id"));
        return mapper.toRecord(inventoryEntity);
    }

    public int getInventoryCount(Integer id) {
        InventoryEntity inventoryEntity = inventoryRepository.findById(id).orElseThrow(() -> new RuntimeException("No inventory found with this id"));
        return inventoryEntity.getCount();
    }

    public Inventory acquireInventoryItem(Integer id, int count) {
        InventoryEntity inventoryEntity= inventoryRepository.findById(id).orElseThrow(() -> new RuntimeException("No inventory found with this id"));
        if (inventoryEntity.getCount() < count) {
            throw new RuntimeException("Not enough items!");
        }
        inventoryEntity.setCount(inventoryEntity.getCount() - count);
        inventoryEntity = inventoryRepository.save(inventoryEntity);
        return mapper.toRecord(inventoryEntity);
    }

    public Inventory saveInventory(Inventory inventory) {
        InventoryEntity inventoryEntity = inventoryRepository.save(mapper.toEntity(inventory));
        return mapper.toRecord(inventoryEntity);
    }
}
