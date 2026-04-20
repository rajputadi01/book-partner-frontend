package com.capg.portal.frontend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class DiscountDto {

    @NotBlank(message = "Discount Type is required")
    private String discountType;

  
    private StoreDto store; 

    private Short lowQty;
    private Short highQty;

    @NotNull(message = "Discount amount is required")
    private BigDecimal discountAmount;

    public DiscountDto() {}

   
    public String getDiscountType() { return discountType; }
    public void setDiscountType(String discountType) { this.discountType = discountType; }
    public StoreDto getStore() { return store; }
    public void setStore(StoreDto store) { this.store = store; }
    public Short getLowQty() { return lowQty; }
    public void setLowQty(Short lowQty) { this.lowQty = lowQty; }
    public Short getHighQty() { return highQty; }
    public void setHighQty(Short highQty) { this.highQty = highQty; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }
}