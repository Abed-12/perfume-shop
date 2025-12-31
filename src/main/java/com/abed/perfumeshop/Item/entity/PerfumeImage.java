package com.abed.perfumeshop.Item.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@Table(name = "perfume_images")
@AllArgsConstructor
@NoArgsConstructor
public class PerfumeImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "BYTEA", nullable = false)
    private byte[] imageData;

    @Column(nullable = false)
    private String mimeType;

    @Column(nullable = false)
    private Long fileSize;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isPrimary = false;

    @Column(nullable = false)
    private Integer displayOrder;

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false, updatable = false)
    private Perfume perfume;

}
