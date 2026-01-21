package com.abed.perfumeshop.Item.entity;

import com.abed.perfumeshop.common.enums.PerfumeSize;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@Table(name = "item_prices")
@AllArgsConstructor
@NoArgsConstructor
public class ItemPrice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Builder.Default
    @Column(nullable = false, updatable = false)
    private LocalDateTime effectiveFrom = LocalDateTime.now();

    private LocalDateTime effectiveTo;

    @Builder.Default
    @Column(nullable = false)
    private Boolean isActive = true;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(nullable = false)
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PerfumeSize perfumeSize;

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false, updatable = false)
    private Item item;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

}
