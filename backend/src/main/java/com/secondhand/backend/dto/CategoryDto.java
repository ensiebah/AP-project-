package com.secondhand.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDto {

    private Long id;

    @NotBlank(message = "Category name is required")
    private String name;

    /** Null means that the category is a top-level category. */
    private Long parentId;

    private String parentName;

    /** Lets clients decide whether they should request the child list. */
    private boolean hasChildren;
}
