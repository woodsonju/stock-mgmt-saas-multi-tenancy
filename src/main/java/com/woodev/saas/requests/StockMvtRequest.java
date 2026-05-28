package com.woodev.saas.requests;

import com.woodev.saas.entities.Product;
import com.woodev.saas.entities.TypeMvt;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StockMvtRequest {
    private TypeMvt typeMvt;
    private Integer quantity;
    private LocalDate dateMvt;
    private String comment;
    private String productId;
}
