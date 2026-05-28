package com.woodev.saas.responses;

import com.woodev.saas.entities.TypeMvt;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StockMvtResponse {
    private TypeMvt typeMvt;
    private Integer quantity;
    private LocalDate dateMvt;
    private String comment;

    //On pourrait ajouter le les détails du produit comme le nom du produit ou sa description
    //mais normalement on n'en a pas besoin parce que lorsqu'on consulte mouvement,
    //on veut consulter les mouvements du produit mais pas juste toutes toutes les mouvements
    //mais les mouvements du stock d'un produit sélectionné
}
