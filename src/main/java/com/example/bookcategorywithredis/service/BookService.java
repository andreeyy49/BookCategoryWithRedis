package com.example.bookcategorywithredis.service;

import com.example.bookcategorywithredis.configuration.properties.AppCacheProperties;
import com.example.bookcategorywithredis.model.Book;
import com.example.bookcategorywithredis.model.Category;
import com.example.bookcategorywithredis.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookService {

    public final BookRepository bookRepository;

    public final CategoryService categoryService;

    public List<Book> findAll() {
        return bookRepository.findAll();
    }

    @Cacheable(cacheNames = AppCacheProperties.CacheNames.DATABASE_BOOK_BY_ID,
            key = "#id")
    public Book findById(Integer id) {
        return bookRepository.findById(id).orElseThrow();
    }

    @Cacheable(cacheNames = AppCacheProperties.CacheNames.DATABASE_BOOK_BY_TITLE_AND_AUTHOR,
            key = "#title + #author")
    public Book findByTitleAndAuthor(String title, String author) {
        List<Book> books = findAll();

        for (Book book : books) {
            if (book.getTitle().equals(title) && book.getAuthor().equals(author)) {
                return book;
            }
        }

        return null;
    }

    @Cacheable(cacheNames = AppCacheProperties.CacheNames.DATABASE_BOOK_BY_CATEGORY,
            key = "#categoryTitle")
    public List<Book> findAllByCategoryTitle(String categoryTitle) {
        List<Book> books = findAll();
        List<Book> returnBooks = new ArrayList<>();

        for (Book book : books) {
            if (book.getCategory().getTitle().equalsIgnoreCase(categoryTitle)) {
                returnBooks.add(book);
            }
        }

        return returnBooks;
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = AppCacheProperties.CacheNames.DATABASE_BOOK_BY_CATEGORY,
                    allEntries = true),
            @CacheEvict(cacheNames = AppCacheProperties.CacheNames.DATABASE_BOOK_BY_TITLE_AND_AUTHOR,
                    allEntries = true),
            @CacheEvict(cacheNames = AppCacheProperties.CacheNames.DATABASE_BOOK_BY_ID,
                    allEntries = true)
    })
    public Book create(Book book, String categoryTitle) {
        String categoryName = categoryTitle.trim().toLowerCase();

        List<Category> categoryList = categoryService.findAll();

        categoryList.forEach(c -> {
            if (c.getTitle().trim().toLowerCase().equals(categoryName)) {
                book.setCategory(c);
            }
        });

        if (book.getCategory() == null) {
            Category category = new Category();
            category.setTitle(categoryTitle);
            category.setBooks(new ArrayList<>());
            categoryService.create(category);
            book.setCategory(category);
        }

        return bookRepository.save(book);
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = AppCacheProperties.CacheNames.DATABASE_BOOK_BY_CATEGORY,
                    key = "#book.getCategory().getTitle()", beforeInvocation = true),
            @CacheEvict(cacheNames = AppCacheProperties.CacheNames.DATABASE_BOOK_BY_TITLE_AND_AUTHOR,
                    key = "#book.getTitle() + #book.getAuthor()", beforeInvocation = true),
            @CacheEvict(cacheNames = AppCacheProperties.CacheNames.DATABASE_BOOK_BY_ID,
                    key = "#book.getId()", beforeInvocation = true)
    })
    public Book update(Book book) {
        String categoryTitle = book.getCategory().getTitle();
        Category categoryInBook = null;

        List<Category> categories = categoryService.findAll();
        for (Category category : categories) {
            if (category.getTitle().trim().toLowerCase().equals(
                    categoryTitle.trim().toLowerCase()
            )) {
                categoryInBook = category;
            }
        }

        if (categoryInBook == null) {
            categoryInBook = new Category();
            categoryInBook.setTitle(categoryTitle);
            categoryInBook.setBooks(new ArrayList<>());
            categoryService.create(categoryInBook);
        }

        Book updatedBook = findById(book.getId());
        updatedBook.setCategory(categoryInBook);
        updatedBook.setAuthor(book.getAuthor());
        updatedBook.setTitle(book.getTitle());

        return bookRepository.save(updatedBook);
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = AppCacheProperties.CacheNames.DATABASE_BOOK_BY_CATEGORY,
                    key = "@bookController.findById(#id).body.categoryTitle"),
            @CacheEvict(cacheNames = AppCacheProperties.CacheNames.DATABASE_BOOK_BY_TITLE_AND_AUTHOR,
                    key = "@bookController.findById(#id).body.title + @bookController.findById(#id).body.author"),
            @CacheEvict(cacheNames = AppCacheProperties.CacheNames.DATABASE_BOOK_BY_ID,
                    key = "#id")
    })
    public void deleteById(Integer id) {
        bookRepository.deleteById(id);
    }
}
