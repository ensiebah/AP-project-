package com.secondhand.backend.controller;

import com.secondhand.backend.dto.CategoryDto;
import com.secondhand.backend.entity.City;
import com.secondhand.backend.repository.CityRepository;
import com.secondhand.backend.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/lookup")
@RequiredArgsConstructor
public class LookupController {

    private final CityRepository cityRepository;
    private final CategoryService categoryService;

    @GetMapping("/cities")
    public List<City> getAllCities() {
        return cityRepository.findAll();
    }

    /** Returns only broad/top-level categories. */
    @GetMapping("/categories")
    public List<CategoryDto> getRootCategories() {
        return categoryService.getRootCategories();
    }

    /** Returns direct subcategories of the selected broad category. */
    @GetMapping("/categories/{parentId}/children")
    public List<CategoryDto> getChildren(@PathVariable Long parentId) {
        return categoryService.getChildrenByParentId(parentId);
    }

    /** Used by the edit form to find the parent of the ad's current category. */
    @GetMapping("/categories/{id}")
    public CategoryDto getCategory(@PathVariable Long id) {
        return categoryService.getCategoryById(id);
    }
}
