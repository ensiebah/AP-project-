package com.secondhand.backend.service.impl;

import com.secondhand.backend.dto.CategoryDto;
import com.secondhand.backend.entity.Category;
import com.secondhand.backend.exception.CategoryNotFoundException;
import com.secondhand.backend.repository.CategoryRepository;
import com.secondhand.backend.repository.AdvertisementRepository;
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
    private final AdvertisementRepository advertisementRepository;

    /** Creates a top-level category. */
    @Override
    public CategoryDto createCategory(String name) {
        String normalizedName = normalizeName(name);
        ensureNameAvailable(normalizedName, null);

        Category category = new Category();
        category.setName(normalizedName);
        return mapToDto(categoryRepository.save(category));
    }

    /** Creates a category below an existing parent category. */
    @Override
    public CategoryDto createSubcategory(String name, Long parentId) {
        String normalizedName = normalizeName(name);
        ensureNameAvailable(normalizedName, null);

        Category parent = categoryRepository.findById(parentId)
                .orElseThrow(() -> new CategoryNotFoundException("Parent category not found"));

        Category category = new Category();
        category.setName(normalizedName);
        category.setParent(parent);
        return mapToDto(categoryRepository.save(category));
    }

    @Override
    public List<CategoryDto> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::mapToDto)
                .toList();
    }

    @Override
    public List<CategoryDto> getRootCategories() {
        return categoryRepository.findByParentIsNullOrderByNameAsc().stream()
                .map(this::mapToDto)
                .toList();
    }

    @Override
    public List<CategoryDto> getChildrenByParentId(Long parentId) {
        // A 404 is preferable to returning an empty list for an invalid id.
        if (!categoryRepository.existsById(parentId)) {
            throw new CategoryNotFoundException("Parent category not found");
        }

        return categoryRepository.findByParentIdOrderByNameAsc(parentId).stream()
                .map(this::mapToDto)
                .toList();
    }

    @Override
    public CategoryDto getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found"));
        return mapToDto(category);
    }

    @Override
    public CategoryDto updateCategoryName(Long id, String name) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found"));
        String normalizedName = normalizeName(name);
        ensureNameAvailable(normalizedName, id);
        category.setName(normalizedName);
        return mapToDto(categoryRepository.save(category));
    }

    /**
     * Deletes only an unused leaf category. This deliberately does not cascade
     * to subcategories or advertisements, which prevents accidental data loss.
     */
    @Override
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found"));

        if (categoryRepository.existsByParentId(id)) {
            throw new IllegalStateException(
                    "Delete or move this category's subcategories before deleting it"
            );
        }
        if (advertisementRepository.existsByCategory(category)) {
            throw new IllegalStateException(
                    "This category is assigned to advertisements and cannot be deleted"
            );
        }
        categoryRepository.delete(category);
    }

    private String normalizeName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Category name is required");
        }
        return name.trim();
    }

    private void ensureNameAvailable(String name, Long currentCategoryId) {
        categoryRepository.findByNameIgnoreCase(name).ifPresent(existing -> {
            if (!existing.getId().equals(currentCategoryId)) {
                throw new IllegalArgumentException("A category with this name already exists");
            }
        });
    }

    private CategoryDto mapToDto(Category category) {
        Category parent = category.getParent();

        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .parentId(parent == null ? null : parent.getId())
                .parentName(parent == null ? null : parent.getName())
                .hasChildren(category.getId() != null && categoryRepository.existsByParentId(category.getId()))
                .build();
    }
}
