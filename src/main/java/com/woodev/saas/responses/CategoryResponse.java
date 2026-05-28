package com.woodev.saas.responses;

import com.woodev.saas.entities.Product;
import lombok.*;

import java.util.List;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CategoryResponse {

    private String name;
    private String description;
    private int nbProducts;
}
