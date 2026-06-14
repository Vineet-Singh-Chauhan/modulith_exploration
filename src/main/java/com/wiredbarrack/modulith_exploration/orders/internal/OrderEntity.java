package com.wiredbarrack.modulith_exploration.orders.internal;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name="orders")
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
class OrderEntity{
    @Id
    @GeneratedValue
    private Integer id;
    private Integer itemId;
    private Integer itemCount;
    private String status;
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    @CreationTimestamp
    private LocalDateTime createdAt;
}
