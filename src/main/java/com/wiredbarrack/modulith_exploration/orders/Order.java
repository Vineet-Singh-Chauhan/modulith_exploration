package com.wiredbarrack.modulith_exploration.orders;

public record Order(
    Integer id,
    Integer itemId,
    Integer itemCount,
    String status){
}
