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
@RequestMapping("/api/static")
@RequiredArgsConstructor
public class StaticDataController {

    private final CityRepository cityRepository;
    private final CategoryRepository categoryRepository;

    // 🏙️ آدرس دریافت تمام شهرهای دیتابیس: GET http://localhost:8080/api/static/cities
    @GetMapping("/cities")
    public List<City> getAllCities() {
        return cityRepository.findAll();
    }

    // 📁 آدرس دریافت تمام دسته‌بندی‌ها: GET http://localhost:8080/api/static/categories
    @GetMapping("/categories")
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }
}