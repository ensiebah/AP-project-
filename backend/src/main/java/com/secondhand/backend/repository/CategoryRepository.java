package com.secondhand.backend.repository;

import com.secondhand.backend.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByNameIgnoreCase(String name);

    boolean existsByName(String name);

    List<Category> findByParentIsNullOrderByNameAsc();

    long countByParentIsNull();

    List<Category> findByParentIdOrderByNameAsc(Long parentId);

    boolean existsByParentId(Long parentId);
}
