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

    public JobMvcController(JobClient jobClient) {
        this.jobClient = jobClient;
    }

    // --- MENU ---
    @GetMapping("/operations")
    public String showOperationsMenu() {
        return "jobs/job-operations";
    }

    // --- 1. CREATE ---
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

    // --- 2. GET ALL ---
    @GetMapping("/get-all")
    public String getAllJobs(Model model) {
        model.addAttribute("jobs", jobClient.getAllJobs());
        model.addAttribute("pageTitle", "All Job Records");
        return "jobs/job-list";
    }

    // --- 3. GET BY ID ---
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

    // --- 4. UPDATE (PUT) ---
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

    // --- 5. PATCH ---
    @GetMapping("/patch")
    public String showPatchIdForm(Model model) {
        model.addAttribute("formTitle", "Patch Job");
        model.addAttribute("actionUrl", "/web/jobs/patch/form");
        return "jobs/job-id-request";
    }

    @GetMapping("/patch/form")
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
    }

    // --- 6. FILTER ---
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

    // --- 7. EMPLOYEES BY JOB ---
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

    // --- 9. ASSIGN EMPLOYEE (PATCH) ---
    @GetMapping("/assign")
    public String showAssignForm() {
        return "jobs/job-assign";
    }

    @PostMapping("/assign/save")
    public String executeAssignment(@RequestParam("jobId") Short jobId, 
                                    @RequestParam("empId") String empId, 
                                    @RequestParam(value = "jobLvl", required = false) Integer jobLvl, 
                                    RedirectAttributes redirectAttributes) {
        try 
        {
            JobLevelUpdateRequest payload = (jobLvl != null) ? new JobLevelUpdateRequest(jobLvl) : null;
            jobClient.assignEmployeeToJob(jobId, empId, payload);
            redirectAttributes.addFlashAttribute("successMessage", "Employee " + empId + " successfully assigned to Job " + jobId);
        } 
        catch (Exception e) 
        {
            redirectAttributes.addFlashAttribute("errorMessage", "Assignment failed. Ensure IDs are correct.");
        }
        return "redirect:/web/jobs/assign";
    }
}