package com.capg.portal.frontend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public class EmployeeDto {

    @NotBlank(message = "Employee ID cannot be blank")
    @Pattern(regexp = "^([A-Z]{3}[1-9][0-9]{4}[FM]|[A-Z]-[A-Z][1-9][0-9]{4}[FM])$", 
             message = "Format: 3 Uppercase OR Letter-Letter, 5 digits (starts 1-9), ends in F/M")
    private String empId;

    @NotBlank(message = "First name is required")
    @Size(max = 20, message = "First name cannot exceed 20 characters")
    private String fname;

    @Size(max = 1, message = "Middle initial must be 1 character")
    private String minit;

    @NotBlank(message = "Last name is required")
    @Size(max = 30, message = "Last name cannot exceed 30 characters")
    private String lname;

    @NotNull(message = "Job assignment is required")
    private JobDto job;

    @NotNull(message = "Job level is required")
    private Integer jobLvl;

    @NotNull(message = "Publisher assignment is required")
    private PublisherDto publisher;

    private LocalDateTime hireDate;

    public EmployeeDto() {}

    // Getters and Setters
    public String getEmpId() { return empId; }
    public void setEmpId(String empId) { this.empId = empId; }

    public String getFname() { return fname; }
    public void setFname(String fname) { this.fname = fname; }

    public String getMinit() { return minit; }
    public void setMinit(String minit) { this.minit = minit; }

    public String getLname() { return lname; }
    public void setLname(String lname) { this.lname = lname; }

    public JobDto getJob() { return job; }
    public void setJob(JobDto job) { this.job = job; }

    public Integer getJobLvl() { return jobLvl; }
    public void setJobLvl(Integer jobLvl) { this.jobLvl = jobLvl; }

    public PublisherDto getPublisher() { return publisher; }
    public void setPublisher(PublisherDto publisher) { this.publisher = publisher; }

    public LocalDateTime getHireDate() { return hireDate; }
    public void setHireDate(LocalDateTime hireDate) { this.hireDate = hireDate; }
}