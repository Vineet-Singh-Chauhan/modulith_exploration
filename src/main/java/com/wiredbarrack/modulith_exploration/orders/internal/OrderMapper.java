package com.wiredbarrack.modulith_exploration.orders.internal;

import com.wiredbarrack.modulith_exploration.orders.Order;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
interface OrderMapper {
    Order toRecord(OrderEntity entity);
    OrderEntity toEntity(Order record);
}
