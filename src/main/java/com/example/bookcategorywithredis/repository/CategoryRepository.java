package com.example.bookcategorywithredis.repository;

import com.example.bookcategorywithredis.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
}
