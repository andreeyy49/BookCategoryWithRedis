package com.example.bookcategorywithredis.mapper;

import com.example.bookcategorywithredis.model.Book;
import com.example.bookcategorywithredis.model.BookResponse;
import com.example.bookcategorywithredis.model.UpsertBookRequest;
import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@DecoratedWith(BookMapperDelegate.class)
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BookMapper {

    Book requestToBook(UpsertBookRequest request);

    @Mapping(target = "id", source = "bookId")
    Book requestToBook(Integer bookId, UpsertBookRequest request);

    BookResponse bookToResponse(Book book);
}
