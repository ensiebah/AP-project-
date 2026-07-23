package com.secondhand.backend.service.impl;

import com.secondhand.backend.dto.CategoryDto;
import com.secondhand.backend.entity.Category;
import com.secondhand.backend.exception.CategoryNotFoundException;
import com.secondhand.backend.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Test
    void createCategory_ShouldCreateSuccessfully() {

        Category category = new Category();
        category.setId(1L);
        category.setName("Electronics");

        when(categoryRepository.existsByName("Electronics")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        CategoryDto result = categoryService.createCategory("Electronics");

        assertNotNull(result);
        assertEquals("Electronics", result.getName());

        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void createCategory_WhenAlreadyExists_ShouldThrowException() {

        when(categoryRepository.existsByName("Electronics"))
                .thenReturn(true);

        assertThrows(
                IllegalArgumentException.class,
                () -> categoryService.createCategory("Electronics")
        );

        verify(categoryRepository, never()).save(any());
    }

    @Test
    void getCategoryById_ShouldReturnCategory() {

        Category category = new Category();
        category.setId(1L);
        category.setName("Books");

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        CategoryDto dto = categoryService.getCategoryById(1L);

        assertEquals("Books", dto.getName());
        assertEquals(1L, dto.getId());
    }

    @Test
    void getCategoryById_WhenNotFound_ShouldThrowException() {

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                CategoryNotFoundException.class,
                () -> categoryService.getCategoryById(1L)
        );
    }

    @Test
    void getAllCategories_ShouldReturnList() {

        Category c1 = new Category();
        c1.setId(1L);
        c1.setName("Books");

        Category c2 = new Category();
        c2.setId(2L);
        c2.setName("Laptop");

        when(categoryRepository.findAll())
                .thenReturn(List.of(c1, c2));

        List<CategoryDto> list =
                categoryService.getAllCategories();

        assertEquals(2, list.size());
    }

    @Test
    void deleteCategory_ShouldDeleteSuccessfully() {

        Category category = new Category();
        category.setId(1L);

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        categoryService.deleteCategory(1L);

        verify(categoryRepository).delete(category);
    }


}