package com.secondhand.backend.controller;

import com.secondhand.backend.entity.Category;
import com.secondhand.backend.entity.City;
import com.secondhand.backend.repository.CategoryRepository;
import com.secondhand.backend.repository.CityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/lookup")
@RequiredArgsConstructor
public class LookupController {

    private final CityRepository cityRepository;
    private final CategoryRepository categoryRepository;

    /**
     * 📥 Endpoint: GET http://localhost:8080/api/lookup/cities
     * Returns a list of all cities in the database.
     */
    @GetMapping("/cities")
    public List<City> getAllCities() {
        return cityRepository.findAll();
    }

    /**
     * 📥 Endpoint: GET http://localhost:8080/api/lookup/categories
     * Returns a list of all categories in the database.
     */
    @GetMapping("/categories")
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }
}