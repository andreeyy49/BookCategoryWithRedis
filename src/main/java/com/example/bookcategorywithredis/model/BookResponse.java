package com.example.bookcategorywithredis.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookResponse {

    private Integer id;

    private String author;

    private String title;

    private String categoryTitle;

    public static BookResponse from(Book book) {
        return new BookResponse(book.getId(), book.getAuthor(), book.getTitle(), book.getCategory().getTitle());
    }
}
