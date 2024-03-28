package com.example.bookcategorywithredis.service;

import com.example.bookcategorywithredis.model.Category;
import com.example.bookcategorywithredis.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<Category> findAll(){
        return categoryRepository.findAll();
    }

    public Category findById(Integer id){
        return categoryRepository.findById(id).orElseThrow();
    }

    public Category findByTitle(String title){return categoryRepository.findAll().stream().filter(c -> c.getTitle().equals(title)).findFirst().orElse(null);}

    public Category create(Category category){
        return categoryRepository.save(category);
    }

    public Category update(Category category){
        Category updatedCategory = findById(category.getId());
        updatedCategory.setBooks(category.getBooks());
        updatedCategory.setTitle(category.getTitle());

        return categoryRepository.save(updatedCategory);
    }

    public void deleteById(Integer id){
        categoryRepository.deleteById(id);
    }
}
