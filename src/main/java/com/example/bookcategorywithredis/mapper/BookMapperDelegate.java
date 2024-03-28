package com.example.bookcategorywithredis.mapper;

import com.example.bookcategorywithredis.model.Book;
import com.example.bookcategorywithredis.model.BookResponse;
import com.example.bookcategorywithredis.model.Category;
import com.example.bookcategorywithredis.model.UpsertBookRequest;
import com.example.bookcategorywithredis.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class BookMapperDelegate implements BookMapper{

    @Autowired
    private CategoryService categoryService;

    @Override
    public Book requestToBook(UpsertBookRequest request) {
        Book book = new Book();
        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());

        Category category = categoryService.findByTitle(request.getCategoryTitle());
        if(category == null){
            category = new Category();
            category.setTitle(request.getCategoryTitle());
        }

        book.setCategory(category);

        return book;
    }

    @Override
    public Book requestToBook(Integer bookId, UpsertBookRequest request) {
        Book book = requestToBook(request);
        book.setId(bookId);

        return book;
    }

    @Override
    public BookResponse bookToResponse(Book book) {
        if(book == null){
            return null;
        }

        BookResponse response = new BookResponse();
        response.setId(book.getId());
        response.setTitle(book.getTitle());
        response.setAuthor(book.getAuthor());
        response.setCategoryTitle(book.getCategory().getTitle());

        return response;
    }
}
