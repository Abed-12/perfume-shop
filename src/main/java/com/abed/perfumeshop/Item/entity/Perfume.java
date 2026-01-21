package com.abed.perfumeshop.Item.entity;

import com.abed.perfumeshop.common.enums.PerfumeType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@Table(name = "perfumes")
@AllArgsConstructor
@NoArgsConstructor
public class Perfume {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PerfumeType perfumeType;

    @Column(nullable = false)
    private String perfumeSeasons;

    @OneToOne(optional = false)
    @JoinColumn(nullable = false, unique = true, updatable = false)
    private Item item;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

}
