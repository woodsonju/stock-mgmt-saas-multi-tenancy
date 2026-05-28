package com.woodev.saas.mappers;

import com.woodev.saas.entities.Category;
import com.woodev.saas.requests.CategoryRequest;
import com.woodev.saas.responses.CategoryResponse;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public Category toEntity(final CategoryRequest request){
        return Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();
    }

    public CategoryResponse toResponse(final Category entity) {
        return CategoryResponse.builder()
                .name(entity.getName())
                .description(entity.getDescription())
                .nbProducts(entity.getProducts().size())
                .build();
    }

}
