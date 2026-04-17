package com.capg.portal.frontend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public class SalesDto {

    @NotNull(message = "Store selection is required")
    private StoreDto store;

    @NotBlank(message = "Order Number is required")
    @Size(max = 20, message = "Order Number cannot exceed 20 characters")
    private String ordNum;

    @NotNull(message = "Title selection is required")
    private TitleDto title;

    private LocalDateTime ordDate;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Short qty;

    @NotBlank(message = "Payment terms are required")
    @Size(max = 12, message = "Payment terms cannot exceed 12 characters")
    private String payterms;

    public SalesDto() {}

    // Getters and Setters
    public StoreDto getStore() { return store; }
    public void setStore(StoreDto store) { this.store = store; }

    public String getOrdNum() { return ordNum; }
    public void setOrdNum(String ordNum) { this.ordNum = ordNum; }

    public TitleDto getTitle() { return title; }
    public void setTitle(TitleDto title) { this.title = title; }

    public LocalDateTime getOrdDate() { return ordDate; }
    public void setOrdDate(LocalDateTime ordDate) { this.ordDate = ordDate; }

    public Short getQty() { return qty; }
    public void setQty(Short qty) { this.qty = qty; }

    public String getPayterms() { return payterms; }
    public void setPayterms(String payterms) { this.payterms = payterms; }
}