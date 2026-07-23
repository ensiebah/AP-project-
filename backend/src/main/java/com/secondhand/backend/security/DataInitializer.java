package com.secondhand.backend.security;

import com.secondhand.backend.entity.Category;
import com.secondhand.backend.entity.City;
import com.secondhand.backend.repository.CategoryRepository;
import com.secondhand.backend.repository.CityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final CityRepository cityRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public void run(String... args) {
        seedCategories();
        seedCities();
    }

    /**
     * Seeds a two-level category tree only when the database has no categories.
     * Existing data is never deleted at start-up because it may already be used
     * by advertisements.
     */
    private void seedCategories() {
        if (categoryRepository.count() > 0) {
            return;
        }

        addCategoryWithChildren("Digital", List.of(
                "Mobile Phones", "Laptops & Tablets", "TV & Audio", "Accessories"
        ));
        addCategoryWithChildren("Home & Kitchen", List.of(
                "Furniture", "Home Appliances", "Decoration & Lighting", "Kitchen"
        ));
        addCategoryWithChildren("Vehicles", List.of(
                "Cars", "Motorcycles", "Vehicle Parts"
        ));
        addCategoryWithChildren("Real Estate", List.of(
                "Residential", "Commercial"
        ));
        addCategoryWithChildren("Fashion", List.of(
                "Women's Clothing", "Men's Clothing", "Shoes & Bags"
        ));
        addCategoryWithChildren("Entertainment", List.of(
                "Books", "Music", "Games & Consoles", "Sports & Leisure"
        ));
        addCategoryWithChildren("Tools & Industrial", List.of(
                "Tools", "Industrial Equipment"
        ));
        addCategoryWithChildren("Services & Jobs", List.of(
                "Services", "Jobs"
        ));
    }

    private void addCategoryWithChildren(String parentName, List<String> childNames) {
        Category parent = new Category();
        parent.setName(parentName);
        Category savedParent = categoryRepository.save(parent);

        for (String childName : childNames) {
            Category child = new Category();
            child.setName(childName);
            child.setParent(savedParent);
            categoryRepository.save(child);
        }
    }

    private void seedCities() {
        if (cityRepository.count() <= 2) {
            cityRepository.deleteAll();

            List<String> cities = List.of(
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
