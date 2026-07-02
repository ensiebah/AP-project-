package com.secondhand.backend.service;

import com.secondhand.backend.dto.CategoryDto;

import java.util.List;

public interface CategoryService {
    CategoryDto createCategory(CategoryDto dto) ;
    List<CategoryDto> getAllCategories() ;
    CategoryDto getCategoryById(Long id) ;
    void deleteCategory(Long id) ;
}
