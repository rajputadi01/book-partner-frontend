package com.capg.portal.frontend.client;

import com.capg.portal.frontend.dto.SalesDto;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.*;

import java.util.List;

public interface SalesClient {

    @GetExchange("/sales")
    List<SalesDto> getAllSales();

    @GetExchange("/sales/{storId}/{ordNum}/{titleId}")
    SalesDto getSaleById(@PathVariable("storId") String storId, @PathVariable("ordNum") String ordNum, @PathVariable("titleId") String titleId);

    @PostExchange("/sales")
    SalesDto createSale(@RequestBody SalesDto sale);

    @PutExchange("/sales/{storId}/{ordNum}/{titleId}")
    SalesDto updateSale(@PathVariable("storId") String storId, @PathVariable("ordNum") String ordNum, @PathVariable("titleId") String titleId, @RequestBody SalesDto sale);

    @PatchExchange("/sales/{storId}/{ordNum}/{titleId}")
    SalesDto patchSale(@PathVariable("storId") String storId, @PathVariable("ordNum") String ordNum, @PathVariable("titleId") String titleId, @RequestBody SalesDto updates);

    @GetExchange("/sales/filter/store")
    List<SalesDto> filterSalesByStore(@RequestParam("storId") String storId);

    @GetExchange("/sales/filter/title")
    List<SalesDto> filterSalesByTitle(@RequestParam("titleId") String titleId);

    @GetExchange("/sales/filter/payterms")
    List<SalesDto> filterSalesByPayterms(@RequestParam("terms") String terms);

    // --- BI Aggregations ---
    @GetExchange("/sales/store/{storId}/total-qty")
    Integer getTotalQtyByStore(@PathVariable("storId") String storId);

    @GetExchange("/sales/store/{storId}/count")
    Long getTransactionCountByStore(@PathVariable("storId") String storId);

    @GetExchange("/sales/title/{titleId}/total-qty")
    Integer getTotalQtyByTitle(@PathVariable("titleId") String titleId);
}