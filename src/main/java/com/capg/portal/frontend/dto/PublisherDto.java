package com.capg.portal.frontend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class PublisherDto {

    @NotBlank(message = "Publisher ID is required")
    @Pattern(regexp = "^(1389|0736|0877|1622|1756|99[0-9]{2})$", message = "ID must be 1389, 0736, 0877, 1622, 1756, or start with 99 followed by 2 digits")
    private String pubId;

    @Size(max = 40, message = "Name cannot exceed 40 characters")
    private String pubName;

    @Size(max = 20, message = "City cannot exceed 20 characters")
    private String city;

    @Size(max = 2, message = "State must be 2 characters")
    private String state;

    @Size(max = 30, message = "Country cannot exceed 30 characters")
    private String country = "USA";

    public PublisherDto() {}

    // Getters and Setters
    public String getPubId() { return pubId; }
    public void setPubId(String pubId) { this.pubId = pubId; }
    public String getPubName() { return pubName; }
    public void setPubName(String pubName) { this.pubName = pubName; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
}