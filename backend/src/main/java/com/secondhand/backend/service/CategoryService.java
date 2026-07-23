package com.secondhand.backend.service;

import com.secondhand.backend.dto.CategoryDto;

import java.util.List;

public interface CategoryService {

    CategoryDto createCategory(String name);

    CategoryDto createSubcategory(String name, Long parentId);

    List<CategoryDto> getAllCategories();

    List<CategoryDto> getRootCategories();

    List<CategoryDto> getChildrenByParentId(Long parentId);

    CategoryDto getCategoryById(Long id);

    CategoryDto updateCategoryName(Long id, String name);

    void deleteCategory(Long id);
}
