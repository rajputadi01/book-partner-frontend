package com.capg.portal.frontend.client;

import com.capg.portal.frontend.dto.*;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.*;

import java.util.List;

public interface AuthorClient {

    @GetExchange("/authors")
    List<AuthorDto> getAllAuthors();

    @GetExchange("/authors/{id}")
    AuthorDto getAuthorById(@PathVariable("id") String id);

    @PostExchange("/authors")
    AuthorDto createAuthor(@RequestBody AuthorDto author);

    @PutExchange("/authors/{id}")
    AuthorDto updateAuthor(@PathVariable("id") String id, @RequestBody AuthorDto author);

    @PatchExchange("/authors/{id}")
    AuthorDto patchAuthor(@PathVariable("id") String id, @RequestBody AuthorDto updates);

    @GetExchange("/authors/filter/contract")
    List<AuthorDto> filterAuthorsByContract(@RequestParam("status") Integer contract);

    @GetExchange("/authors/filter/city")
    List<AuthorDto> filterAuthorsByCity(@RequestParam("city") String city);

    @GetExchange("/authors/filter/state")
    List<AuthorDto> filterAuthorsByState(@RequestParam("state") String state);

    @GetExchange("/authors/{id}/title-authors")
    List<TitleAuthorDto> getTitleAuthorsByAuthor(@PathVariable("id") String id);

    @GetExchange("/authors/{id}/titles")
    List<TitleDto> getTitlesByAuthor(@PathVariable("id") String id);

    @GetExchange("/authors/{id}/publishers")
    List<PublisherDto> getPublishersByAuthor(@PathVariable("id") String id);

    @GetExchange("/authors/{id}/stores")
    List<StoreDto> getStoresByAuthor(@PathVariable("id") String id);
}