package com.abed.perfumeshop.order.entity;

import com.abed.perfumeshop.coupon.entity.Coupon;
import com.abed.perfumeshop.customer.entity.Customer;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@Table(name = "authenticated_orders")
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticatedOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(nullable = false, unique = true, updatable = false)
    private Order order;

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false, updatable = false)
    private Customer customer;

    @ManyToOne
    private Coupon coupon;

}
