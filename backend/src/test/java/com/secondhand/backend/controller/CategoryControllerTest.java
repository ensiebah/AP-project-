package com.secondhand.backend.controller;

import com.secondhand.backend.dto.CategoryDto;
import com.secondhand.backend.service.CategoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
@AutoConfigureMockMvc(addFilters = false)
class CategoryControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    CategoryService categoryService;

    @Test
    void createCategory_ShouldReturnCreatedCategory() throws Exception {

        CategoryDto dto = CategoryDto.builder()
                .id(1L)
                .name("Laptop")
                .build();

        when(categoryService.createCategory(anyString()))
                .thenReturn(dto);

        mockMvc.perform(post("/categories")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("Laptop"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Laptop"));
    }

    @Test
    void getAllCategories_ShouldReturnList() throws Exception {

        CategoryDto dto = CategoryDto.builder()
                .id(1L)
                .name("Laptop")
                .build();

        when(categoryService.getAllCategories())
                .thenReturn(List.of(dto));

        mockMvc.perform(get("/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Laptop"));
    }

    @Test
    void getCategoryById_ShouldReturnCategory() throws Exception {

        CategoryDto dto = CategoryDto.builder()
                .id(1L)
                .name("Laptop")
                .build();

        when(categoryService.getCategoryById(1L))
                .thenReturn(dto);

        mockMvc.perform(get("/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Laptop"));
    }

    @Test
    void deleteCategory_ShouldReturnOk() throws Exception {

        doNothing().when(categoryService).deleteCategory(1L);

        mockMvc.perform(delete("/categories/1"))
                .andExpect(status().isOk());

        verify(categoryService).deleteCategory(1L);
    }
}