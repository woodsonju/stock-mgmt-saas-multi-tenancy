package com.woodev.saas.requests;

import com.woodev.saas.entities.Product;
import com.woodev.saas.entities.TypeMvt;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StockMvtRequest {

    @NotBlank(message = "Type of movement should not be empty")
    private TypeMvt typeMvt;

    @Positive(message = "Quantity should be a positive number")
    private Integer quantity;

    //On peut pas dire par exemple c'est un mouvement pour la semaine prochaine
    //il faut que ce soit maximum la date du jour minimum,
    //et le minimum peut être n'importe quelle date mais ça ne peut
    //pas par exemple être la date de la semaine prochaine donc on va utiliser past or present
    @NotNull(message = "Date of movement should not be empty")
    @PastOrPresent(message = "Date of movement should be in the past or present")
    private LocalDate dateMvt;

    private String comment;

    @NotBlank(message = "Product ID should not be empty")
    private String productId;
}
