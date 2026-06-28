package com.wiredbarrack.modulith_exploration.orders.dto;

import lombok.*;
import org.springframework.modulith.events.Externalized;

@Externalized("orders.placed::#{#this.getId()}")
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderPlaced {
    private Integer id;
}
