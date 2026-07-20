package com.secondhand.backend.service.impl;

import com.secondhand.backend.dto.CategoryDto;

import com.secondhand.backend.entity.Category;

import com.secondhand.backend.exception.CategoryNotFoundException;

import com.secondhand.backend.repository.CategoryRepository;

import com.secondhand.backend.service.CategoryService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service implementation responsible for category management.
 */
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    /**
     * Creates a new category.
     *
     * @param name category name
     * @return created category
     */
    @Override
    public CategoryDto createCategory(
            String name
    ) {

        if (categoryRepository.existsByName(name)) {
            throw new IllegalArgumentException(
                    "Category already exists"
            );
        }

        Category category = new Category();

        category.setName(name);

        Category savedCategory =
                categoryRepository.save(category);

        return mapToDto(savedCategory);
    }

    /**
     * Retrieves all categories.
     *
     * @return list of categories
     */
    @Override
    public List<CategoryDto> getAllCategories() {

        return categoryRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    /**
     * Retrieves a category by its identifier.
     *
     * @param id category identifier
     * @return category information
     */
    @Override
    public CategoryDto getCategoryById(
            Long id
    ) {

        Category category =
                categoryRepository.findById(id)
                        .orElseThrow(() ->
                                new CategoryNotFoundException(
                                        "Category not found"
                                ));

        return mapToDto(category);
    }

    /**
     * Deletes a category.
     *
     * @param id category identifier
     */
    @Override
    public void deleteCategory(
            Long id
    ) {

        Category category =
                categoryRepository.findById(id)
                        .orElseThrow(() ->
                                new CategoryNotFoundException(
                                        "Category not found"
                                ));

        categoryRepository.delete(category);
    }

    private CategoryDto mapToDto(
            Category category
    ) {

        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }
}