package com.woodev.saas.mappers;

import com.woodev.saas.entities.Category;
import com.woodev.saas.entities.Product;
import com.woodev.saas.requests.ProductRequest;
import com.woodev.saas.responses.ProductResponse;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {
    public Product toEntity(final ProductRequest request){
        return Product.builder()
                .name(request.getName())
                .reference(request.getReference())
                .description(request.getDescription())
                .price(request.getPrice())
                .alertThreshold(request.getAlertThreshold())
                .category(Category.builder()
                        .id(request.getCategoryId())
                        .build())
                .build();
    }

    // AvailableQuantity : On peut le calculer après par exemple en créant une méthode
    // dans la classe Mouvement du Stock pour pouvoir calculer la quantité disponible.
    public ProductResponse toResponse(final Product entity) {
        return ProductResponse.builder()
                .name(entity.getName())
                .reference(entity.getReference())
                .description(entity.getDescription())
                .price(entity.getPrice())
                .alertThreshold(entity.getAlertThreshold())
                .categoryName(entity.getCategory().getName())
                //.availableQuantity() to be later implemented
                .build();
    }
}
