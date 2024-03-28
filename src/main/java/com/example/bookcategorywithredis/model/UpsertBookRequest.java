package com.example.bookcategorywithredis.model;

import lombok.Data;

@Data
public class UpsertBookRequest {

    private String author;

    private String title;

    private String categoryTitle;
}
