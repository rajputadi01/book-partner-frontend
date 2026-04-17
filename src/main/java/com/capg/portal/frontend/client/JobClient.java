package com.capg.portal.frontend.client;

import com.capg.portal.frontend.dto.JobDto;
import com.capg.portal.frontend.dto.EmployeeDto;
import com.capg.portal.frontend.dto.PublisherDto;
import com.capg.portal.frontend.dto.JobLevelUpdateRequest;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PatchExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;

import java.util.List;

public interface JobClient {

    @GetExchange("/jobs")
    List<JobDto> getAllJobs();

    @GetExchange("/jobs/{id}")
    JobDto getJobById(@PathVariable("id") Short id);

    @PostExchange("/jobs")
    JobDto createJob(@RequestBody JobDto job);

    @PutExchange("/jobs/{id}")
    JobDto updateJob(@PathVariable("id") Short id, @RequestBody JobDto job);

    @PatchExchange("/jobs/{id}")
    JobDto patchJob(@PathVariable("id") Short id, @RequestBody JobDto updates);

    @GetExchange("/jobs/filter")
    List<JobDto> filterJobsByLevels(@RequestParam("minLvl") Integer minLvl, @RequestParam("maxLvl") Integer maxLvl);

    @GetExchange("/jobs/{id}/employees")
    List<EmployeeDto> getEmployeesByJob(@PathVariable("id") Short id);

    @GetExchange("/jobs/{id}/publishers")
    List<PublisherDto> getPublishersByJob(@PathVariable("id") Short id);

    @PatchExchange("/jobs/{jobId}/employees/{empId}")
    EmployeeDto assignEmployeeToJob(@PathVariable("jobId") Short jobId, @PathVariable("empId") String empId, @RequestBody JobLevelUpdateRequest request);
}