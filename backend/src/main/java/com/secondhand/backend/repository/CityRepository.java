package com.secondhand.backend.repository;

import com.secondhand.backend.entity.City;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CityRepository extends JpaRepository<City , Long> {
    Optional<City> findByNameIgnoreCase(String name) ;
}
