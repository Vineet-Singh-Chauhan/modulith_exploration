package com.wiredbarrack.modulith_exploration.inventory;

import lombok.Builder;

@Builder
public record Inventory(
        Integer id,
        String name,
        String category,
        Integer count
) {
}
