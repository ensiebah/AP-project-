package com.secondhand.backend;

import com.secondhand.backend.entity.Category;
import com.secondhand.backend.entity.City;
import com.secondhand.backend.repository.CategoryRepository;
import com.secondhand.backend.repository.CityRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import java.util.List;

@SpringBootApplication
public class SecondHandBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(SecondHandBackendApplication.class, args);
    }

    /**
     * 🚀 این متد به محض اجرای برنامه، دیتابیس H2 را با شهرهای اولیه و دسته‌بندی‌ها پر می‌کند.
     * به دلیل استفاده از فیلد نام یکتا (Unique)، ابتدا بررسی می‌کنیم دیتابیس خالی باشد.
     */
    @Bean
    public CommandLineRunner initDatabase(CategoryRepository categoryRepository, CityRepository cityRepository) {
        return args -> {
            // تزریق خودکار دسته‌بندی‌ها در صورت خالی بودن
            if (categoryRepository.count() == 0) {
                categoryRepository.saveAll(List.of(
                        new Category(null, "Electronics"),
                        new Category(null, "Vehicles"),
                        new Category(null, "Home & Kitchen"),
                        new Category(null, "Books & Hobbies"),
                        new Category(null, "Fashion & Clothing")
                ));
                System.out.println("🌱 Default categories successfully seeded into database.");
            }

            // تزریق خودکار شهرهای ایران در صورت خالی بودن
            if (cityRepository.count() == 0) {
                cityRepository.saveAll(List.of(
                        new City(null, "Tehran"),
                        new City(null, "Mashhad"),
                        new City(null, "Isfahan"),
                        new City(null, "Shiraz"),
                        new City(null, "Tabriz"),
                        new City(null, "Karaj")
                ));
                System.out.println("🌱 Default cities successfully seeded into database.");
            }
        };
    }
}