package com.secondhand.backend.controller;

import com.secondhand.backend.dto.CategoryDto;
import com.secondhand.backend.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService ;
    /**
     * Creates a new category.
     *
     * @param dto the category information
     * @return the created category
     */
    @PostMapping
    public CategoryDto creatCategory(
            @RequestBody CategoryDto dto
    ){
        return categoryService.createCategory(
                dto.getName()
        );
    }
    /**
     * Retrieves all available categories.
     *
     * @return a list of categories
     */
    @GetMapping
    public List<CategoryDto> getAllCategories(){
        return categoryService.getAllCategories() ;
    }

    /**
     * Retrieves a category by its unique identifier.
     *
     * @param id the category ID
     * @return the requested category
     */
    @GetMapping("/{id}")
    public CategoryDto getCategoryById(@PathVariable Long id){
        return categoryService.getCategoryById(id) ;
    }

    /**
     * Deletes a category by its unique identifier.
     *
     * @param id the category ID
     */
    @DeleteMapping("/{id}")
    public void deleteCategory(@PathVariable Long id){
        categoryService.deleteCategory(id);
    }

}
