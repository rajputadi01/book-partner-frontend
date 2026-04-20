package com.capg.portal.frontend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class AuthorDto {

    @NotBlank(message = "Author ID is required")
    @Pattern(regexp = "^[0-9]{3}-[0-9]{2}-[0-9]{4}$", message = "Format must be: XXX-XX-XXXX")
    private String auId;

    @NotBlank(message = "Last name is required")
    @Size(max = 40, message = "Last name cannot exceed 40 characters")
    private String auLname;

    @NotBlank(message = "First name is required")
    @Size(max = 20, message = "First name cannot exceed 20 characters")
    private String auFname;

    @Pattern(regexp = "^([0-9]{3} [0-9]{3}-[0-9]{4}|UNKNOWN)$", message = "Format: XXX XXX-XXXX or UNKNOWN")
    private String phone = "UNKNOWN";

    private String address;
    private String city;

    @Size(max = 2, message = "State must be exactly 2 characters")
    private String state;

    @Pattern(regexp = "^([0-9]{5})?$", message = "Zip must be exactly 5 digits")
    private String zip;

    @NotNull(message = "Contract status is required")
    private Integer contract = 1;

    public AuthorDto() {}

    public String getAuId() { return auId; }
    public void setAuId(String auId) { this.auId = auId; }
    public String getAuLname() { return auLname; }
    public void setAuLname(String auLname) { this.auLname = auLname; }
    public String getAuFname() { return auFname; }
    public void setAuFname(String auFname) { this.auFname = auFname; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public String getZip() { return zip; }
    public void setZip(String zip) { this.zip = zip; }
    public Integer getContract() { return contract; }
    public void setContract(Integer contract) { this.contract = contract; }
}