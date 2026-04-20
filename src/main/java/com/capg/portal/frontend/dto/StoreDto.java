package com.capg.portal.frontend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class StoreDto {

    @NotBlank(message = "Store ID is required")
    @Size(min = 4, max = 4, message = "Store ID must be exactly 4 characters")
    private String storId;

    @NotBlank(message = "Store name is required")
    @Size(max = 40, message = "Name cannot exceed 40 characters")
    private String storName;

    @Size(max = 40, message = "Address cannot exceed 40 characters")
    private String storAddress;

    @Size(max = 20, message = "City cannot exceed 20 characters")
    private String city;

    @Size(max = 2, message = "State must be exactly 2 characters")
    private String state;

    @Pattern(regexp = "^([0-9]{5})?$", message = "Zip must be exactly 5 digits")
    private String zip;

    public StoreDto() {}

    public String getStorId() { return storId; }
    public void setStorId(String storId) { this.storId = storId; }
    public String getStorName() { return storName; }
    public void setStorName(String storName) { this.storName = storName; }
    public String getStorAddress() { return storAddress; }
    public void setStorAddress(String storAddress) { this.storAddress = storAddress; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public String getZip() { return zip; }
    public void setZip(String zip) { this.zip = zip; }
}