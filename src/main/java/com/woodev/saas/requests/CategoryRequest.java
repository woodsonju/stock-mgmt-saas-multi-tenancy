package com.woodev.saas.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CategoryRequest {

   // @NotNull(message = "Category name should not be empty")
    @NotBlank(message = "Category name should not be empty")
    @Size(min = 3, max = 255, message = "Category name should be between 3 and 255 characters")
    private String name;

    private String description;
}
