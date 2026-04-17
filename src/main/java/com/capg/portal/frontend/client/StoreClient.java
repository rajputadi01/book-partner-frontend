package com.capg.portal.frontend.client;

import com.capg.portal.frontend.dto.*;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.*;

import java.util.List;

public interface StoreClient {

    @GetExchange("/stores")
    List<StoreDto> getAllStores();

    @GetExchange("/stores/{id}")
    StoreDto getStoreById(@PathVariable("id") String id);

    @PostExchange("/stores")
    StoreDto createStore(@RequestBody StoreDto store);

    @PutExchange("/stores/{id}")
    StoreDto updateStore(@PathVariable("id") String id, @RequestBody StoreDto store);

    @PatchExchange("/stores/{id}")
    StoreDto patchStore(@PathVariable("id") String id, @RequestBody StoreDto updates);

    @GetExchange("/stores/filter/city")
    List<StoreDto> filterStoresByCity(@RequestParam("city") String city);

    @GetExchange("/stores/filter/state")
    List<StoreDto> filterStoresByState(@RequestParam("state") String state);

    @GetExchange("/stores/{id}/sales")
    List<SalesDto> getSalesByStore(@PathVariable("id") String id);

    @GetExchange("/stores/{id}/titles")
    List<TitleDto> getTitlesByStore(@PathVariable("id") String id);

    @GetExchange("/stores/{id}/publishers")
    List<PublisherDto> getPublishersByStore(@PathVariable("id") String id);

    @GetExchange("/stores/{id}/authors")
    List<AuthorDto> getAuthorsByStore(@PathVariable("id") String id);

    @GetExchange("/stores/{id}/discounts")
    List<DiscountDto> getDiscountsByStore(@PathVariable("id") String id);
}