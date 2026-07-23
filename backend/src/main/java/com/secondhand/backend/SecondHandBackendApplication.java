package com.secondhand.backend;

import com.secondhand.backend.entity.City;
import com.secondhand.backend.entity.Role;
import com.secondhand.backend.entity.User;
import com.secondhand.backend.repository.CityRepository;
import com.secondhand.backend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@SpringBootApplication
public class SecondHandBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(SecondHandBackendApplication.class, args);
    }

    @Bean
    public CommandLineRunner initDatabase(
            CityRepository cityRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {
            // Categories are seeded in DataInitializer, the single source of
            // truth for the parent/subcategory tree.
            if (cityRepository.count() == 0) {
                cityRepository.saveAll(List.of(
                        new City(null, "Tehran"),
                        new City(null, "Shiraz")
                ));
            }

            userRepository.findByUserName("admin").ifPresent(userRepository::delete);

            User admin = new User();
            admin.setUserName("admin");
            admin.setPassWord(passwordEncoder.encode("password"));
            admin.setFullName("Project Admin");
            admin.setRole(Role.ADMIN);
            admin.setBlocked(false);

            userRepository.save(admin);
            System.out.println("Successfully seeded Admin with native Project Encoder! Pass: password");
        };
    }
}
