package com.wiredbarrack.modulith_exploration.inventory.internal;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
interface InventoryRepository extends CrudRepository<InventoryEntity, Integer> {
}
