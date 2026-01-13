package com.abed.perfumeshop.coupon.entity;

import com.abed.perfumeshop.customer.entity.Customer;
import com.abed.perfumeshop.order.entity.Order;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@Table(
        name = "coupon_usages",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_coupon_customer",
                        columnNames = {"coupon_id", "customer_id"}
                )
        }
)
@AllArgsConstructor
@NoArgsConstructor
public class CouponUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false, updatable = false)
    private Customer customer;

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false, updatable = false)
    private Coupon coupon;

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false, updatable = false)
    private Order order;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime usedAt;

}
