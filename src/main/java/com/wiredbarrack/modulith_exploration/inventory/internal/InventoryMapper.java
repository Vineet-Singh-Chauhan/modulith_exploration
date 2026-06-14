package com.wiredbarrack.modulith_exploration.inventory.internal;

import com.wiredbarrack.modulith_exploration.inventory.Inventory;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
interface InventoryMapper {
    Inventory toRecord(InventoryEntity entity);
    InventoryEntity toEntity(Inventory record);
}
