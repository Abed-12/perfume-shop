package com.abed.perfumeshop.order.entity;

import com.abed.perfumeshop.Item.entity.Item;
import com.abed.perfumeshop.common.enums.PerfumeSize;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@Table(name = "order_items")
@AllArgsConstructor
@NoArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private BigDecimal unitPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PerfumeSize perfumeSize;

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false, updatable = false)
    private Order order;

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false, updatable = false)
    private Item item;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

}
