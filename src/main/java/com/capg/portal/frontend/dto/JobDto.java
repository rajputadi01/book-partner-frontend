package com.capg.portal.frontend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class JobDto {
    private Short jobId;

    @NotBlank(message = "Job description is required")
    @Size(max = 50, message = "Description cannot exceed 50 characters")
    private String jobDesc = "New Position - title not formalized yet";

    @NotNull(message = "Minimum level is required")
    @Min(value = 10, message = "Minimum level must be at least 10")
    private Integer minLvl;

    @NotNull(message = "Maximum level is required")
    @Max(value = 250, message = "Maximum level cannot exceed 250")
    private Integer maxLvl;

    // Default Constructor
    public JobDto() {}

    // Getters and Setters
    public Short getJobId() { return jobId; }
    public void setJobId(Short jobId) { this.jobId = jobId; }

    public String getJobDesc() { return jobDesc; }
    public void setJobDesc(String jobDesc) { this.jobDesc = jobDesc; }

    public Integer getMinLvl() { return minLvl; }
    public void setMinLvl(Integer minLvl) { this.minLvl = minLvl; }

    public Integer getMaxLvl() { return maxLvl; }
    public void setMaxLvl(Integer maxLvl) { this.maxLvl = maxLvl; }
}