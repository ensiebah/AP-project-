package com.secondhand.backend.controller;

import com.secondhand.backend.dto.CategoryDto;
import com.secondhand.backend.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * Creates a root category when parentId is null, otherwise a subcategory.
     */
    @PostMapping
    public CategoryDto createCategory(@RequestBody CategoryDto dto) {
        if (dto.getParentId() == null) {
            return categoryService.createCategory(dto.getName());
        }
        return categoryService.createSubcategory(dto.getName(), dto.getParentId());
    }

    @GetMapping
    public List<CategoryDto> getAllCategories() {
        return categoryService.getAllCategories();
    }

    @GetMapping("/roots")
    public List<CategoryDto> getRootCategories() {
        return categoryService.getRootCategories();
    }

    @GetMapping("/{id}/children")
    public List<CategoryDto> getChildren(@PathVariable Long id) {
        return categoryService.getChildrenByParentId(id);
    }

    @GetMapping("/{id}")
    public CategoryDto getCategoryById(@PathVariable Long id) {
        return categoryService.getCategoryById(id);
    }

    @DeleteMapping("/{id}")
    public void deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
    }
}
