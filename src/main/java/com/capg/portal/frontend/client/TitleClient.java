package com.capg.portal.frontend.client;

import com.capg.portal.frontend.dto.*;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

public interface TitleClient {

    @GetExchange("/titles")
    List<TitleDto> getAllTitles();

    @GetExchange("/titles/{id}")
    TitleDto getTitleById(@PathVariable("id") String id);

    @PostExchange("/titles")
    TitleDto createTitle(@RequestBody TitleDto title);

    @PutExchange("/titles/{id}")
    TitleDto updateTitle(@PathVariable("id") String id, @RequestBody TitleDto title);

    @PatchExchange("/titles/{id}")
    TitleDto patchTitle(@PathVariable("id") String id, @RequestBody TitleDto updates);

    @GetExchange("/titles/filter/price")
    List<TitleDto> filterTitlesByPrice(@RequestParam("maxPrice") Double maxPrice);

    @GetExchange("/titles/filter/type")
    List<TitleDto> filterTitlesByType(@RequestParam("type") String type);

    @GetExchange("/titles/filter/publisher")
    List<TitleDto> filterTitlesByPublisher(@RequestParam("pubId") String pubId);

    @GetExchange("/titles/filter/date")
    List<TitleDto> filterTitlesByDateBefore(@RequestParam("beforeDate") LocalDateTime beforeDate);

    @GetExchange("/titles/{id}/publisher")
    PublisherDto getPublisherByTitle(@PathVariable("id") String id);

    @GetExchange("/titles/{id}/sales")
    List<SalesDto> getSalesByTitle(@PathVariable("id") String id);

    @GetExchange("/titles/{id}/royalties")
    List<RoyaltyScheduleDto> getRoyaltiesByTitle(@PathVariable("id") String id);

    @GetExchange("/titles/{id}/title-authors")
    List<TitleAuthorDto> getTitleAuthorsByTitle(@PathVariable("id") String id);

    @GetExchange("/titles/{id}/authors")
    List<AuthorDto> getAuthorsByTitle(@PathVariable("id") String id);

    @GetExchange("/titles/{id}/stores")
    List<StoreDto> getStoresByTitle(@PathVariable("id") String id);
}