package com.secondhand.backend.security;

import com.secondhand.backend.entity.Category;
import com.secondhand.backend.entity.City;
import com.secondhand.backend.repository.CategoryRepository;
import com.secondhand.backend.repository.CityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final CityRepository cityRepository;
    private final CategoryRepository categoryRepository;

    /**
     * 🏁 Responsibility: This method runs automatically when Spring Boot finishes starting up.
     */
    @Override
    public void run(String... args) throws Exception {
        seedCategories();
        seedCities();
    }

    /**
     * 🟢 Responsibility: Populates the database with standard marketplace categories if empty.
     */
    private void seedCategories() {
        if (categoryRepository.count() <= 2) {
            categoryRepository.deleteAll();

            List<String> categories = Arrays.asList(
                    "Digital & Electronics",
                    "Vehicles & Spares",
                    "Real Estate",
                    "Home & Kitchen",
                    "Apparel & Fashion",
                    "Books & Hobbies",
                    "Sports & Leisure",
                    "Toys & Games",
                    "Tools & Industrial",
                    "Services & Jobs"
            );

            for (String catName : categories) {
                Category category = new Category();
                category.setName(catName);
                categoryRepository.save(category);
            }

        }
    }

    /**
     * 🟢 Responsibility: Populates the database with major Iranian cities if empty.
     */
    private void seedCities() {
        if (cityRepository.count() <= 2) {
            cityRepository.deleteAll();

            List<String> cities = Arrays.asList(
                    "Tehran", "Mashhad", "Isfahan", "Karaj", "Shiraz", "Tabriz", "Qom", "Ahvaz",
                    "Kermanshah", "Urmia", "Rasht", "Zahedan", "Hamadan", "Kerman", "Yazd", "Ardabil",
                    "Bandar Abbas", "Arak", "Zanjan", "Sanandaj", "Qazvin", "Khorramabad", "Gorgan",
                    "Sari", "Shahrekord", "Semnan", "Bojnourd", "Birjand", "Ilam", "Yasuj", "Bushehr"
            );

            for (String cityName : cities) {
                City city = new City();
                city.setName(cityName);
                cityRepository.save(city);
            }

        }
    }
}