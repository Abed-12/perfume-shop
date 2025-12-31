package com.abed.perfumeshop.order.entity;

import com.abed.perfumeshop.customer.entity.Customer;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@Table(name = "guest_orders")
@AllArgsConstructor
@NoArgsConstructor
public class GuestOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String phoneNumber;

    private String alternativePhoneNumber;

    @Column(nullable = false)
    private String governorate;

    @Column(nullable = false)
    private String address;

    @ManyToOne
    private Customer claimedByCustomer;

    private LocalDateTime claimedAt;

    @OneToOne(optional = false)
    @JoinColumn(nullable = false, unique = true, updatable = false)
    private Order order;

}
