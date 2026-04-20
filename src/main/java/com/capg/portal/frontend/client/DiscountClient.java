package com.capg.portal.frontend.client;

import com.capg.portal.frontend.dto.DiscountDto;
import com.capg.portal.frontend.dto.StoreDto;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.*;

import java.math.BigDecimal;
import java.util.List;

public interface DiscountClient {

    @GetExchange("/discounts")
    List<DiscountDto> getAllDiscounts();

    @GetExchange("/discounts/{type}")
    DiscountDto getDiscountByType(@PathVariable("type") String type);

    @PostExchange("/discounts")
    DiscountDto createDiscount(@RequestBody DiscountDto discount);

    @PutExchange("/discounts/{type}")
    DiscountDto updateDiscount(@PathVariable("type") String type, @RequestBody DiscountDto discount);

    @PatchExchange("/discounts/{type}")
    DiscountDto patchDiscount(@PathVariable("type") String type, @RequestBody DiscountDto updates);

    @GetExchange("/discounts/filter/qty")
    List<DiscountDto> filterDiscountsByQty(@RequestParam("minQty") Integer minQty, @RequestParam("maxQty") Integer maxQty);

    @GetExchange("/discounts/filter/amount")
    List<DiscountDto> filterDiscountsByAmount(@RequestParam("maxAmount") BigDecimal maxAmount);

    @GetExchange("/discounts/filter/store")
    List<DiscountDto> filterDiscountsByStore(@RequestParam("storId") String storId);

  
    @GetExchange("/discounts/{type}/store")
    StoreDto getStoreByDiscountType(@PathVariable("type") String type);
}