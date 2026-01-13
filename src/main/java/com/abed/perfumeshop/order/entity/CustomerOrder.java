package com.abed.perfumeshop.order.entity;

import com.abed.perfumeshop.common.enums.Governorate;
import com.abed.perfumeshop.coupon.entity.Coupon;
import com.abed.perfumeshop.customer.entity.Customer;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@Table(name = "customer_orders")
@AllArgsConstructor
@NoArgsConstructor
public class CustomerOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Governorate governorate;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String phoneNumber;

    private String alternativePhoneNumber;

    @OneToOne(optional = false)
    @JoinColumn(nullable = false, unique = true, updatable = false)
    private Order order;

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false, updatable = false)
    private Customer customer;

    @ManyToOne
    private Coupon coupon;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

}
