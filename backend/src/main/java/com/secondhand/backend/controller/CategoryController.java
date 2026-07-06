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
    @PostMapping
    public CategoryDto creatCategory(
            @RequestBody CategoryDto dto
    ){
        return categoryService.createCategory(
                dto.getName()
        );
    }
    @GetMapping
    public List<CategoryDto> getAllCategories(){
        return categoryService.getAllCategories() ;
    }
    @GetMapping("/{id}")
    public CategoryDto getCategoryById(@PathVariable Long id){
        return categoryService.getCategoryById(id) ;
    }
    @DeleteMapping("/{id}")
    public void deleteCategory(@PathVariable Long id){
        categoryService.deleteCategory(id);
    }

}
