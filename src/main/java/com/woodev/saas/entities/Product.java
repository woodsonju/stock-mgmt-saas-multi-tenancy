package com.woodev.saas.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(name = "products")
public class Product extends AbstractEntity{

    @Column(name = "product_name", nullable = false)
    private String name;

    @Column(name = "reference", nullable = false, unique = true)
    private String reference;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "alert_threshold", nullable = false)
    private Integer alertThreshold;

    @Column(name = "price", nullable = false)
    private BigDecimal price;


    //La quantité on peut l'ignorer, car on peut la calculer depuis les mouvements du stock,
    //private Integer quantity;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @OneToMany(mappedBy = "product")
    private List<StockMvt> stockMovements;


}
