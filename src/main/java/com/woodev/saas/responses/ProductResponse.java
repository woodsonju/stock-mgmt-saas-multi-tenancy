package com.woodev.saas.responses;


import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductResponse {
    private String name;
    private String reference;
    private String description;
    private Integer alertThreshold;
    private BigDecimal price;
    private String categoryName;
    //Qauntité du stock disponible
    //Cela peut nous aider après dans la partie interface utilisateur
    //Par exemple chez amazon lorsque vous cherchez un produit il vous affiche une information
    //il ne reste que 3 ou 4 ou 5 produits, cela pousse les gens à acheter le produit
    private int availableQuantity;
}
