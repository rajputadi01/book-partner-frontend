package com.capg.portal.frontend.dto;

import jakarta.validation.constraints.NotNull;

public class RoyaltyScheduleDto {

    private Integer royschedId;

    @NotNull(message = "Title selection is required")
    private TitleDto title;

    @NotNull(message = "Low range is required")
    private Integer lorange;

    @NotNull(message = "High range is required")
    private Integer hirange;

    @NotNull(message = "Royalty percentage is required")
    private Integer royalty;

    public RoyaltyScheduleDto() {}

    // Getters and Setters
    public Integer getRoyschedId() { return royschedId; }
    public void setRoyschedId(Integer royschedId) { this.royschedId = royschedId; }

    public TitleDto getTitle() { return title; }
    public void setTitle(TitleDto title) { this.title = title; }

    public Integer getLorange() { return lorange; }
    public void setLorange(Integer lorange) { this.lorange = lorange; }

    public Integer getHirange() { return hirange; }
    public void setHirange(Integer hirange) { this.hirange = hirange; }

    public Integer getRoyalty() { return royalty; }
    public void setRoyalty(Integer royalty) { this.royalty = royalty; }
}