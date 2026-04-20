package com.capg.portal.frontend.dto;

import jakarta.validation.constraints.NotNull;

public class TitleAuthorDto {

    @NotNull(message = "Author selection is required")
    private AuthorDto author;

    @NotNull(message = "Title selection is required")
    private TitleDto title;

    private Integer auOrd;
    private Integer royaltyPer;

    public TitleAuthorDto() {}

 
    public AuthorDto getAuthor() { return author; }
    public void setAuthor(AuthorDto author) { this.author = author; }

    public TitleDto getTitle() { return title; }
    public void setTitle(TitleDto title) { this.title = title; }

    public Integer getAuOrd() { return auOrd; }
    public void setAuOrd(Integer auOrd) { this.auOrd = auOrd; }

    public Integer getRoyaltyPer() { return royaltyPer; }
    public void setRoyaltyPer(Integer royaltyPer) { this.royaltyPer = royaltyPer; }
}