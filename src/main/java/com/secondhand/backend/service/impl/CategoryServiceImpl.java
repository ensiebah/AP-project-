package com.secondhand.backend.service.impl;

import com.secondhand.backend.dto.CategoryDto;
import com.secondhand.backend.entity.Category;
import com.secondhand.backend.repository.CategoryRepository;
import com.secondhand.backend.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
@RequiredArgsConstructor
@Transactional
@Service
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    @Override
    public CategoryDto createCategory(CategoryDto dto) {
        if (categoryRepository.existsByName(dto.getName())){
            throw new RuntimeException("Category already exists");
        }
        Category category = new Category() ;
        category.setName(dto.getName());
        Category saved = categoryRepository.save(category) ;
        return mapToDto(saved);
    }

    @Override
    public List<CategoryDto> getAllCategories() {
        return categoryRepository.findAll().stream().map(this::mapToDto).toList();
    }

    @Override
    public CategoryDto getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(()->new RuntimeException("Category not found")) ;
        return mapToDto(category);
    }

    @Override
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(()->new RuntimeException("Category not found")) ;
        categoryRepository.delete(category);
    }

    private CategoryDto mapToDto(Category category){
        return CategoryDto.builder().
                id(category.getId())
                .name(category.getName())
                .build() ;
    }
}
