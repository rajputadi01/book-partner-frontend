package com.capg.portal.frontend.controller;

import com.capg.portal.frontend.client.JobClient;
import com.capg.portal.frontend.dto.JobDto;
import com.capg.portal.frontend.dto.JobLevelUpdateRequest;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/web/jobs")
public class JobMvcController {

    private final JobClient jobClient;
<<<<<<< Updated upstream
=======
    private final int PAGE_SIZE = 5; 
>>>>>>> Stashed changes

    public JobMvcController(JobClient jobClient) {
        this.jobClient = jobClient;
    }

    // --- MENU ---
    @GetMapping("/operations")
    public String showOperationsMenu() {
        return "jobs/job-operations";
    }

    
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("job", new JobDto());
        model.addAttribute("formTitle", "Create New Job");
        model.addAttribute("actionUrl", "/web/jobs/create/save");
        model.addAttribute("isUpdate", false);
        return "jobs/job-form";
    }

    @PostMapping("/create/save")
    public String saveNewJob(@Valid @ModelAttribute("job") JobDto job, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("formTitle", "Create New Job");
            model.addAttribute("actionUrl", "/web/jobs/create/save");
            model.addAttribute("isUpdate", false);
            return "jobs/job-form";
        }
        jobClient.createJob(job);
        return "redirect:/web/jobs/get-all";
    }

   
    @GetMapping("/get-all")
    public String getAllJobs(Model model) {
        model.addAttribute("jobs", jobClient.getAllJobs());
        model.addAttribute("pageTitle", "All Job Records");
        return "jobs/job-list";
    }

    
    @GetMapping("/get-by-id")
    public String showGetByIdForm(Model model) {
        model.addAttribute("formTitle", "Fetch Job by ID");
        model.addAttribute("actionUrl", "/web/jobs/get-by-id/result");
        return "jobs/job-id-request";
    }

    @GetMapping("/get-by-id/result")
    public String getJobByIdResult(@RequestParam("id") Short id, Model model) {
        model.addAttribute("job", jobClient.getJobById(id));
        return "jobs/job-details";
    }

    
    @GetMapping("/update")
    public String showUpdateIdForm(Model model) 
    {
        model.addAttribute("formTitle", "Update Job (PUT)");
        model.addAttribute("actionUrl", "/web/jobs/update/form");
        return "jobs/job-id-request";
    }

    @GetMapping("/update/form")
    public String showUpdateForm(@RequestParam("id") Short id, Model model) {
        model.addAttribute("job", jobClient.getJobById(id));
        model.addAttribute("formTitle", "Update Job Data");
        model.addAttribute("actionUrl", "/web/jobs/update/save");
        model.addAttribute("isUpdate", true);
        return "jobs/job-form";
    }

    @PostMapping("/update/save")
    public String saveUpdatedJob(@Valid @ModelAttribute("job") JobDto job, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("formTitle", "Update Job Data");
            model.addAttribute("actionUrl", "/web/jobs/update/save");
            model.addAttribute("isUpdate", true);
            return "jobs/job-form";
        }
        jobClient.updateJob(job.getJobId(), job);
        return "redirect:/web/jobs/get-all";
    }

<<<<<<< Updated upstream
    // --- 5. PATCH ---
=======
   
>>>>>>> Stashed changes
    @GetMapping("/patch")
    public String showPatchIdForm(Model model) {
        model.addAttribute("formTitle", "Patch Job");
        model.addAttribute("actionUrl", "/web/jobs/patch/form");
        return "jobs/job-id-request";
    }

    @GetMapping("/patch/form")
<<<<<<< Updated upstream
    public String showPatchForm(@RequestParam("id") Short id, Model model) {
        model.addAttribute("job", jobClient.getJobById(id));
        model.addAttribute("formTitle", "Patch Job Data");
        model.addAttribute("actionUrl", "/web/jobs/patch/save");
        model.addAttribute("isUpdate", true);
        return "jobs/job-form";
    }

    @PostMapping("/patch/save")
    public String savePatchedJob(@ModelAttribute("job") JobDto job) {
        jobClient.patchJob(job.getJobId(), job);
        return "redirect:/web/jobs/get-all";
=======
    public String showPatchForm(@RequestParam("id") Short id, Model model, RedirectAttributes redirectAttributes) {
        try {
            
            model.addAttribute("currentJob", jobClient.getJobById(id));
            
            
            JobDto patchDto = new JobDto();
            patchDto.setJobId(id); 
            
            model.addAttribute("job", patchDto);
            model.addAttribute("formTitle", "Patch Job Data");
            model.addAttribute("actionUrl", "/web/jobs/patch/save");
            return "jobs/job-patch"; 
            
        } catch (HttpStatusCodeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", extractErrorMessage(e));
            return "redirect:/web/jobs/patch";
        }
    }

    @PostMapping("/patch/save")
    public String savePatchedJob(@ModelAttribute("job") JobDto job, RedirectAttributes redirectAttributes) {
        
        
        if (job.getJobDesc() != null && job.getJobDesc().trim().isEmpty()) {
            job.setJobDesc(null);
        }

        
        if (job.getMinLvl() != null && job.getMaxLvl() != null && job.getMinLvl() >= job.getMaxLvl()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Min Level must be strictly less than Max Level.");
            return "redirect:/web/jobs/patch/form?id=" + job.getJobId();
        }

        try {
            jobClient.patchJob(job.getJobId(), job);
            return "redirect:/web/jobs/get-all";
        } catch (HttpStatusCodeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", extractErrorMessage(e));
            return "redirect:/web/jobs/patch/form?id=" + job.getJobId();
        }
>>>>>>> Stashed changes
    }

  
    @GetMapping("/filter")
    public String showFilterForm() {
        return "jobs/job-filter";
    }

    @GetMapping("/filter/result")
    public String filterJobsResult(@RequestParam("minLvl") Integer minLvl, @RequestParam("maxLvl") Integer maxLvl, Model model) {
        model.addAttribute("jobs", jobClient.filterJobsByLevels(minLvl, maxLvl));
        model.addAttribute("pageTitle", "Filtered Jobs (" + minLvl + " to " + maxLvl + ")");
        return "jobs/job-list";
    }

    
    @GetMapping("/employees")
    public String showEmployeesIdForm(Model model) {
        model.addAttribute("formTitle", "Find Employees by Job ID");
        model.addAttribute("actionUrl", "/web/jobs/employees/result");
        return "jobs/job-id-request";
    }

    @GetMapping("/employees/result")
    public String getEmployeesResult(@RequestParam("id") Short id, Model model) {
        model.addAttribute("targetId", id);
        model.addAttribute("employees", jobClient.getEmployeesByJob(id));
        return "jobs/job-employees";
    }

    // --- 8. PUBLISHERS BY JOB ---
    @GetMapping("/publishers")
    public String showPublishersIdForm(Model model) {
        model.addAttribute("formTitle", "Find Publishers by Job ID");
        model.addAttribute("actionUrl", "/web/jobs/publishers/result");
        return "jobs/job-id-request";
    }

    @GetMapping("/publishers/result")
    public String getPublishersResult(@RequestParam("id") Short id, Model model) {
        model.addAttribute("targetId", id);
        model.addAttribute("publishers", jobClient.getPublishersByJob(id));
        return "jobs/job-publishers";
    }

    
    @GetMapping("/assign")
    public String showAssignForm() {
        return "jobs/job-assign";
    }

    @PostMapping("/assign/save")
    public String executeAssignment(@RequestParam("jobId") Short jobId, 
                                    @RequestParam("empId") String empId, 
                                    @RequestParam(value = "jobLvl", required = false) Integer jobLvl, 
                                    RedirectAttributes redirectAttributes) {
<<<<<<< Updated upstream
        try 
        {
            JobLevelUpdateRequest payload = (jobLvl != null) ? new JobLevelUpdateRequest(jobLvl) : null;
=======
        try {
            
            if (jobLvl != null) {
                JobDto targetJob = jobClient.getJobById(jobId); // Fetch job details
                if (jobLvl < targetJob.getMinLvl() || jobLvl > targetJob.getMaxLvl()) {
                    redirectAttributes.addFlashAttribute("errorMessage", 
                        "Validation Error: Job Level " + jobLvl + " is out of bounds for Job " + jobId + 
                        " (Allowed: " + targetJob.getMinLvl() + " to " + targetJob.getMaxLvl() + ").");
                    return "redirect:/web/jobs/assign";
                }
            }

            
            JobLevelUpdateRequest payload = new JobLevelUpdateRequest(jobLvl);
>>>>>>> Stashed changes
            jobClient.assignEmployeeToJob(jobId, empId, payload);
            redirectAttributes.addFlashAttribute("successMessage", "Employee " + empId + " successfully assigned to Job " + jobId);
        } 
        catch (Exception e) 
        {
            redirectAttributes.addFlashAttribute("errorMessage", "Assignment failed. Ensure IDs are correct.");
        }
        return "redirect:/web/jobs/assign";
    }
<<<<<<< Updated upstream
=======

        
    private <T> List<T> paginateList(List<T> list, int page, int size, Model model) {
        if (list == null || list.isEmpty()) {
            model.addAttribute("currentPage", 1);
            model.addAttribute("totalPages", 1);
            model.addAttribute("totalItems", 0);
            return list;
        }
        int totalItems = list.size();
        int totalPages = (int) Math.ceil((double) totalItems / size);
        if (page < 1) page = 1;
        if (page > totalPages) page = totalPages;

        int start = (page - 1) * size;
        int end = Math.min(start + size, totalItems);

        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalItems", totalItems);
        return list.subList(start, end);
    }

    private void sortJobs(List<JobDto> list, String sortBy, String dir) {
        if ("jobDesc".equals(sortBy)) list.sort(Comparator.comparing(JobDto::getJobDesc));
        else if ("minLvl".equals(sortBy)) list.sort(Comparator.comparing(JobDto::getMinLvl));
        else if ("maxLvl".equals(sortBy)) list.sort(Comparator.comparing(JobDto::getMaxLvl));
        else list.sort(Comparator.comparing(JobDto::getJobId));
        if ("desc".equals(dir)) Collections.reverse(list);
    }

    private void sortEmployees(List<EmployeeDto> list, String sortBy, String dir) {
        if ("fname".equals(sortBy)) list.sort(Comparator.comparing(EmployeeDto::getFname));
        else if ("lname".equals(sortBy)) list.sort(Comparator.comparing(EmployeeDto::getLname));
        else if ("jobLvl".equals(sortBy)) list.sort(Comparator.comparing(EmployeeDto::getJobLvl));
        else list.sort(Comparator.comparing(EmployeeDto::getEmpId));
        if ("desc".equals(dir)) Collections.reverse(list);
    }

    private void sortPublishers(List<PublisherDto> list, String sortBy, String dir) {
        if ("pubName".equals(sortBy)) list.sort(Comparator.comparing(PublisherDto::getPubName));
        else if ("city".equals(sortBy)) list.sort(Comparator.comparing(PublisherDto::getCity));
        else list.sort(Comparator.comparing(PublisherDto::getPubId));
        if ("desc".equals(dir)) Collections.reverse(list);
    }

    private String extractErrorMessage(HttpStatusCodeException e) {
        try {
            com.fasterxml.jackson.databind.JsonNode node = new com.fasterxml.jackson.databind.ObjectMapper().readTree(e.getResponseBodyAsString());
            return node.has("message") ? node.get("message").asText() : e.getMessage();
        } catch (Exception ex) {
            return "Validation or connection error occurred.";
        }
    }
>>>>>>> Stashed changes
}