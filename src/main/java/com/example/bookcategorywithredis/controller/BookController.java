package com.example.bookcategorywithredis.controller;

import com.example.bookcategorywithredis.mapper.BookMapper;
import com.example.bookcategorywithredis.model.Book;
import com.example.bookcategorywithredis.model.BookResponse;
import com.example.bookcategorywithredis.model.Category;
import com.example.bookcategorywithredis.model.UpsertBookRequest;
import com.example.bookcategorywithredis.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/book")
public class BookController {

    private final BookService bookService;
    private final BookMapper bookMapper;

    @GetMapping
    public ResponseEntity<List<BookResponse>> findAll() {
        return ResponseEntity.ok(bookService.findAll().stream().map(bookMapper::bookToResponse).toList());
    }

    @GetMapping("/findById/{id}")
    public ResponseEntity<BookResponse> findById(@PathVariable Integer id){
        return ResponseEntity.ok(bookMapper.bookToResponse(bookService.findById(id)));
    }

    @GetMapping("/findByTitleAndAuthor/{title}/{author}")
    public ResponseEntity<BookResponse> findByTitleAndAuthor(@PathVariable String title, @PathVariable String author) {
        return ResponseEntity.ok(bookMapper.bookToResponse(
                bookService.findByTitleAndAuthor(title, author)
        ));
    }

    @GetMapping("/findByCategoryTitle/{categoryTitle}")
    public ResponseEntity<List<BookResponse>> findByCategoryTitle(@PathVariable String categoryTitle) {
        return ResponseEntity.ok(bookService.findAllByCategoryTitle(categoryTitle).stream().map(bookMapper::bookToResponse).toList());
    }

    @PostMapping
    public ResponseEntity<BookResponse> create(@RequestBody UpsertBookRequest request) {
        Book book = new Book();
        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());

        bookService.create(book, request.getCategoryTitle());

        return ResponseEntity.status(HttpStatus.CREATED).body(
                bookMapper.bookToResponse(book)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookResponse> update(@PathVariable("id") Integer bookId, @RequestBody UpsertBookRequest request) {
        Book book = bookService.update(bookMapper.requestToBook(bookId, request));

        return ResponseEntity.ok(bookMapper.bookToResponse(book));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Integer id) {
        bookService.deleteById(id);

        return ResponseEntity.noContent().build();
    }
}
