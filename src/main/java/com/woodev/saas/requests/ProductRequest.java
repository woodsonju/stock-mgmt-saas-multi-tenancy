package com.woodev.saas.requests;

import com.woodev.saas.entities.Category;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductRequest {
    private String name;
    private String reference;
    private String description;
    private Integer alertThreshold;
    private BigDecimal price;
    private String categoryId;
}
