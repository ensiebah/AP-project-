package com.secondhand.backend;

import com.secondhand.backend.entity.Category;
import com.secondhand.backend.entity.City;
import com.secondhand.backend.entity.User;
import com.secondhand.backend.entity.Role;
import com.secondhand.backend.repository.CategoryRepository;
import com.secondhand.backend.repository.CityRepository;
import com.secondhand.backend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder; // 🟢 این را حتماً ایمپورت کن

import java.util.List;

@SpringBootApplication
public class SecondHandBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(SecondHandBackendApplication.class, args);
    }

    @Bean
    public CommandLineRunner initDatabase(
            CategoryRepository categoryRepository,
            CityRepository cityRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) { // 🟢 تزریق پاسورد انکودر رسمی پروژه شما

        return args -> {
            // کدهای مربوط به کاتگوری و شهر دست‌نخورده باقی بمانند...
            if (categoryRepository.count() == 0) {
                categoryRepository.saveAll(List.of(new Category(null, "Digital Electronics"), new Category(null, "Home Appliances")));
            }
            if (cityRepository.count() == 0) {
                cityRepository.saveAll(List.of(new City(null, "Tehran"), new City(null, "Shiraz")));
            }

            // 👮‍♂️ اصلاح بخش ساخت ادمین
            // ابتدا کاربر قبلی که ارور می‌داد را پاک می‌کنیم تا تداخلی ایجاد نشود
            userRepository.findByUserName("admin").ifPresent(userRepository::delete);

            // حالا ادمین جدید را با انکودر استاندارد خود پروژه می‌سازیم
            User admin = new User();
            admin.setUserName("admin");

            // 🔥 این خط کلید حل مشکل است: خود پروژه رمز "password" را انکود می‌کند
            admin.setPassWord(passwordEncoder.encode("password"));

            admin.setFullName("Project Admin");
            admin.setRole(Role.ADMIN);
            admin.setBlocked(false);

            userRepository.save(admin);
            System.out.println("👮‍♂️ Successfully seeded Admin with native Project Encoder! Pass: password");
        };
    }
}