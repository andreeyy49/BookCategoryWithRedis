package com.example.bookcategorywithredis.controller;

import com.example.bookcategorywithredis.AbstractTest;
import com.example.bookcategorywithredis.model.BookResponse;
import com.example.bookcategorywithredis.model.UpsertBookRequest;
import net.javacrumbs.jsonunit.JsonAssert;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class BookControllerTest extends AbstractTest {

    @Test
    public void whenGetAllBooks_thenReturnBookList() throws Exception {
        assertTrue(redisTemplate.keys("*").isEmpty());

        String actualResponse = mockMvc.perform(get("/api/book"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String expectedResponse = objectMapper.writeValueAsString(bookService.findAll().stream().map(bookMapper::bookToResponse).toList());
        JsonAssert.assertJsonEquals(expectedResponse, actualResponse);
    }

    @Test
    public void whenGetAllBooksByCategoryTitle_thenReturnBookList() throws Exception {
        assertTrue(redisTemplate.keys("*").isEmpty());

        String actualResponse = mockMvc.perform(get("/api/book/Category1"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String expectedResponse = objectMapper.writeValueAsString(bookService.findAllByCategoryTitle("Category1").stream().map(bookMapper::bookToResponse).toList());

        assertFalse(redisTemplate.keys("*").isEmpty());
        JsonAssert.assertJsonEquals(expectedResponse, actualResponse);
    }

    @Test
    public void whenGetBookByTitleAndAuthor_thenReturnOneBook() throws Exception {
        assertTrue(redisTemplate.keys("*").isEmpty());

        String actualResponse = mockMvc.perform(get("/api/book/TitleBook1/Author1"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String expectedResponse = objectMapper.writeValueAsString(bookMapper.bookToResponse(
                bookService.findByTitleAndAuthor("TitleBook1", "Author1")));

        assertFalse(redisTemplate.keys("*").isEmpty());
        JsonAssert.assertJsonEquals(expectedResponse, actualResponse);
    }

    @Test
    public void whenCreateBook_thenCreateBookAndEvictCache() throws Exception {
        assertTrue(redisTemplate.keys("*").isEmpty());
        assertEquals(3, bookRepository.count());

        mockMvc.perform(get("/api/book/Category1"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertFalse(redisTemplate.keys("*").isEmpty());

        UpsertBookRequest request = new UpsertBookRequest();
        request.setAuthor("newAuthor");
        request.setTitle("newTitleBook");
        request.setCategoryTitle("newCategoryTitle");
        String actualResponse = mockMvc.perform(post("/api/book")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String expectedResponse = objectMapper.writeValueAsString(new BookResponse(1, "newAuthor", "newTitleBook", "newCategoryTitle"));

        assertTrue(redisTemplate.keys("*").isEmpty());
        assertEquals(4, bookRepository.count());

        JsonAssert.assertJsonEquals(expectedResponse, actualResponse, JsonAssert.whenIgnoringPaths("id"));
    }

    @Test
    public void whenUpdateBook_thenUpdateBookAndEvictCache() throws Exception {
        assertTrue(redisTemplate.keys("*").isEmpty());

        mockMvc.perform(get("/api/book/Category1"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertFalse(redisTemplate.keys("*").isEmpty());

        UpsertBookRequest request = new UpsertBookRequest();
        request.setAuthor("updateAuthor");
        request.setTitle("updateTitleBook");
        request.setCategoryTitle("updateCategoryTitle");
        String actualResponse = mockMvc.perform(put("/api/book/{id}", UPDATED_ID.toString())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String expectedResponse = objectMapper.writeValueAsString(new BookResponse(UPDATED_ID, "updateAuthor", "updateTitleBook", "updateCategoryTitle"));

        assertTrue(redisTemplate.keys("*").isEmpty());

        JsonAssert.assertJsonEquals(expectedResponse, actualResponse, JsonAssert.whenIgnoringPaths("id"));
    }

    @Test
    public void whenDeleteEntityById_thenDeleteEntityByIdAndEvictCache() throws Exception {
        assertTrue(redisTemplate.keys("*").isEmpty());
        assertEquals(3, bookRepository.count());

        mockMvc.perform(get("/api/book/Category1"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertFalse(redisTemplate.keys("*").isEmpty());

        mockMvc.perform(delete("/api/book/" + UPDATED_ID))
                .andExpect(status().isNoContent());

        assertTrue(redisTemplate.keys("*").isEmpty());
        assertEquals(2, bookRepository.count());
    }
}
