package com.capg.portal.frontend.client;

import com.capg.portal.frontend.dto.EmployeeDto;
import com.capg.portal.frontend.dto.JobDto;
import com.capg.portal.frontend.dto.PublisherDto;
import com.capg.portal.frontend.dto.TitleDto;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.*;

import java.util.List;

public interface EmployeeClient 
{
    @GetExchange("/employees")
    List<EmployeeDto> getAllEmployees();

    @GetExchange("/employees/{id}")
    EmployeeDto getEmployeeById(@PathVariable("id") String id);

    @PostExchange("/employees")
    EmployeeDto createEmployee(@RequestBody EmployeeDto employee);

    @PutExchange("/employees/{id}")
    EmployeeDto updateEmployee(@PathVariable("id") String id, @RequestBody EmployeeDto employee);

    @PatchExchange("/employees/{id}")
    EmployeeDto patchEmployee(@PathVariable("id") String id, @RequestBody EmployeeDto updates);

    @GetExchange("/employees/filter/job")
    List<EmployeeDto> filterByJob(@RequestParam("jobId") Short jobId);

    @GetExchange("/employees/filter/job-level")
    List<EmployeeDto> filterByJobLevel(@RequestParam("maxLvl") Integer maxLvl);

    @GetExchange("/employees/filter/publisher")
    List<EmployeeDto> filterByPublisher(@RequestParam("pubId") String pubId);

    @GetExchange("/employees/{id}/job")
    JobDto getEmployeeJob(@PathVariable("id") String id);

    @GetExchange("/employees/{id}/publisher")
    PublisherDto getEmployeePublisher(@PathVariable("id") String id);

    @GetExchange("/employees/{id}/publisher/titles")
    List<TitleDto> getEmployeePublisherTitles(@PathVariable("id") String id);
}