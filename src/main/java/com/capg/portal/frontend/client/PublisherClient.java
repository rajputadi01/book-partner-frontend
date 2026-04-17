package com.capg.portal.frontend.client;

import com.capg.portal.frontend.dto.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.service.annotation.*;
import java.util.List;

public interface PublisherClient {

    @GetExchange("/publishers")
    List<PublisherDto> getAllPublishers();

    @GetExchange("/publishers/{id}")
    PublisherDto getPublisherById(@PathVariable("id") String id);

    @PostExchange("/publishers")
    PublisherDto createPublisher(@RequestBody PublisherDto publisher);

    @PutExchange("/publishers/{id}")
    PublisherDto updatePublisher(@PathVariable("id") String id, @RequestBody PublisherDto publisher);

    @PatchExchange("/publishers/{id}")
    PublisherDto patchPublisher(@PathVariable("id") String id, @RequestBody PublisherDto updates);

    @GetExchange("/publishers/filter/city")
    List<PublisherDto> getPublishersByCity(@RequestParam("city") String city);

    @GetExchange("/publishers/filter/state")
    List<PublisherDto> getPublishersByState(@RequestParam("state") String state);

    @GetExchange("/publishers/filter/country")
    List<PublisherDto> getPublishersByCountry(@RequestParam("country") String country);

    @GetExchange("/publishers/{id}/employees")
    List<EmployeeDto> getEmployeesByPublisher(@PathVariable("id") String id);

    @GetExchange("/publishers/{id}/titles")
    List<TitleDto> getTitlesByPublisher(@PathVariable("id") String id);

    @GetExchange("/publishers/{id}/authors")
    List<AuthorDto> getAuthorsByPublisher(@PathVariable("id") String id);

    @GetExchange("/publishers/{id}/stores")
    List<StoreDto> getStoresByPublisher(@PathVariable("id") String id);
}