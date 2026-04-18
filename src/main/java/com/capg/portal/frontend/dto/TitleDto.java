package com.capg.portal.frontend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public class TitleDto {

    @NotBlank(message = "Title ID is required")
    @Size(max = 10, message = "Title ID cannot exceed 10 characters")
    private String titleId;

    @NotBlank(message = "Book Name is required")
    @Size(max = 80, message = "Name cannot exceed 80 characters")
    private String titleName;

    private String type = "UNDECIDED";

    // Nested Publisher to map the Foreign Key relationship in the Thymeleaf form
    private PublisherDto publisher;

    private Double price;
    private Double advance;
    private Integer royalty;
    private Integer ytdSales;

    @Size(max = 200, message = "Notes cannot exceed 200 characters")
    private String notes;

    private LocalDateTime pubdate;

    public TitleDto() {}

    // Getters and Setters
    public String getTitleId() { return titleId; }
    public void setTitleId(String titleId) { this.titleId = titleId; }

    public String getTitleName() { return titleName; }
    public void setTitleName(String titleName) { this.titleName = titleName; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public PublisherDto getPublisher() { return publisher; }
    public void setPublisher(PublisherDto publisher) { this.publisher = publisher; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public Double getAdvance() { return advance; }
    public void setAdvance(Double advance) { this.advance = advance; }

    public Integer getRoyalty() { return royalty; }
    public void setRoyalty(Integer royalty) { this.royalty = royalty; }

    public Integer getYtdSales() { return ytdSales; }
    public void setYtdSales(Integer ytdSales) { this.ytdSales = ytdSales; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getPubdate() { return pubdate; }
    public void setPubdate(LocalDateTime pubdate) { this.pubdate = pubdate; }
}