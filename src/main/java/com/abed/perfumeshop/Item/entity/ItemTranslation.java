package com.abed.perfumeshop.Item.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@Table(name = "item_translations",
        uniqueConstraints = @UniqueConstraint(
            name = "uk_item_locale",
            columnNames = {"item_id", "locale"}
        ))
@AllArgsConstructor
@NoArgsConstructor
public class ItemTranslation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 5)
    private String locale;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false, updatable = false)
    private Item item;

}
