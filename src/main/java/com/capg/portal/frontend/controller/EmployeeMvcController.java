package com.capg.portal.frontend.controller;

import com.capg.portal.frontend.client.EmployeeClient;
import com.capg.portal.frontend.dto.EmployeeDto;
import com.capg.portal.frontend.dto.JobDto;
import com.capg.portal.frontend.dto.PublisherDto;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/web/employees")
public class EmployeeMvcController {

    private final EmployeeClient employeeClient;

    public EmployeeMvcController(EmployeeClient employeeClient) {
        this.employeeClient = employeeClient;
    }

    @GetMapping("/operations")
    public String showOperationsMenu() {
        return "employees/employee-operations";
    }

    // --- CRUD ---
    @GetMapping("/get-all")
    public String getAll(Model model) {
        model.addAttribute("employees", employeeClient.getAllEmployees());
        model.addAttribute("pageTitle", "All Employee Records");
        return "employees/employee-list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        EmployeeDto dto = new EmployeeDto();
        dto.setJob(new JobDto());
        dto.setPublisher(new PublisherDto());
        
        model.addAttribute("employee", dto);
        model.addAttribute("formTitle", "Register New Employee");
        model.addAttribute("actionUrl", "/web/employees/create/save");
        model.addAttribute("isUpdate", false);
        return "employees/employee-form";
    }

    @PostMapping("/create/save")
    public String saveCreate(@Valid @ModelAttribute("employee") EmployeeDto employee, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("formTitle", "Register New Employee");
            model.addAttribute("actionUrl", "/web/employees/create/save");
            model.addAttribute("isUpdate", false);
            return "employees/employee-form";
        }
        try {
            employeeClient.createEmployee(employee);
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error: Ensure the ID is unique and the Job Level falls within the Job's Min/Max limits.");
            model.addAttribute("formTitle", "Register New Employee");
            model.addAttribute("actionUrl", "/web/employees/create/save");
            model.addAttribute("isUpdate", false);
            return "employees/employee-form";
        }
        return "redirect:/web/employees/get-all";
    }

    @GetMapping("/get-by-id")
    public String showIdRequest(Model model) {
        model.addAttribute("formTitle", "Fetch Employee by ID");
        model.addAttribute("actionUrl", "/web/employees/get-by-id/result");
        return "employees/employee-id-request";
    }

    @GetMapping("/get-by-id/result")
    public String getIdResult(@RequestParam("id") String id, Model model) {
        model.addAttribute("employee", employeeClient.getEmployeeById(id));
        return "employees/employee-details";
    }

    @GetMapping("/update")
    public String showUpdateIdRequest(Model model) {
        model.addAttribute("formTitle", "Update Employee (PUT)");
        model.addAttribute("actionUrl", "/web/employees/update/form");
        return "employees/employee-id-request";
    }

    @GetMapping("/update/form")
    public String showUpdateForm(@RequestParam("id") String id, Model model) {
        EmployeeDto employee = employeeClient.getEmployeeById(id);
        if (employee.getJob() == null) employee.setJob(new JobDto());
        if (employee.getPublisher() == null) employee.setPublisher(new PublisherDto());

        model.addAttribute("employee", employee);
        model.addAttribute("formTitle", "Update Employee Data");
        model.addAttribute("actionUrl", "/web/employees/update/save");
        model.addAttribute("isUpdate", true);
        return "employees/employee-form";
    }

    @PostMapping("/update/save")
    public String saveUpdate(@Valid @ModelAttribute("employee") EmployeeDto employee, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("formTitle", "Update Employee Data");
            model.addAttribute("actionUrl", "/web/employees/update/save");
            model.addAttribute("isUpdate", true);
            return "employees/employee-form";
        }
        try {
            employeeClient.updateEmployee(employee.getEmpId(), employee);
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error: Job Level is out of bounds for the selected Job ID.");
            model.addAttribute("formTitle", "Update Employee Data");
            model.addAttribute("actionUrl", "/web/employees/update/save");
            model.addAttribute("isUpdate", true);
            return "employees/employee-form";
        }
        return "redirect:/web/employees/get-all";
    }

    @GetMapping("/patch")
    public String showPatchIdRequest(Model model) {
        model.addAttribute("formTitle", "Patch Employee");
        model.addAttribute("actionUrl", "/web/employees/patch/form");
        return "employees/employee-id-request";
    }

    @GetMapping("/patch/form")
    public String showPatchForm(@RequestParam("id") String id, Model model) {
        EmployeeDto employee = employeeClient.getEmployeeById(id);
        if (employee.getJob() == null) employee.setJob(new JobDto());
        if (employee.getPublisher() == null) employee.setPublisher(new PublisherDto());

        model.addAttribute("employee", employee);
        model.addAttribute("formTitle", "Patch Employee Data");
        model.addAttribute("actionUrl", "/web/employees/patch/save");
        model.addAttribute("isUpdate", true);
        return "employees/employee-form";
    }

    @PostMapping("/patch/save")
    public String savePatch(@ModelAttribute("employee") EmployeeDto employee, Model model) {
        try {
            employeeClient.patchEmployee(employee.getEmpId(), employee);
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error: Job Level constraint violated.");
            model.addAttribute("formTitle", "Patch Employee Data");
            model.addAttribute("actionUrl", "/web/employees/patch/save");
            model.addAttribute("isUpdate", true);
            return "employees/employee-form";
        }
        return "redirect:/web/employees/get-all";
    }

    // --- FILTERS ---
    @GetMapping("/filter/job")
    public String filterJob(Model model) {
        model.addAttribute("formTitle", "Filter by Job ID");
        model.addAttribute("actionUrl", "/web/employees/filter/job/result");
        model.addAttribute("paramName", "jobId");
        model.addAttribute("inputType", "number");
        return "employees/employee-single-param-request";
    }

    @GetMapping("/filter/job/result")
    public String filterJobResult(@RequestParam("jobId") Short jobId, Model model) {
        model.addAttribute("employees", employeeClient.filterByJob(jobId));
        model.addAttribute("pageTitle", "Employees Assigned to Job ID: " + jobId);
        return "employees/employee-list";
    }

    @GetMapping("/filter/job-level")
    public String filterJobLevel(Model model) {
        model.addAttribute("formTitle", "Filter by Max Job Level");
        model.addAttribute("actionUrl", "/web/employees/filter/job-level/result");
        model.addAttribute("paramName", "maxLvl");
        model.addAttribute("inputType", "number");
        return "employees/employee-single-param-request";
    }

    @GetMapping("/filter/job-level/result")
    public String filterJobLevelResult(@RequestParam("maxLvl") Integer maxLvl, Model model) {
        model.addAttribute("employees", employeeClient.filterByJobLevel(maxLvl));
        model.addAttribute("pageTitle", "Employees with Job Level < " + maxLvl);
        return "employees/employee-list";
    }

    @GetMapping("/filter/publisher")
    public String filterPublisher(Model model) {
        model.addAttribute("formTitle", "Filter by Publisher ID");
        model.addAttribute("actionUrl", "/web/employees/filter/publisher/result");
        model.addAttribute("paramName", "pubId");
        model.addAttribute("inputType", "text");
        return "employees/employee-single-param-request";
    }

    @GetMapping("/filter/publisher/result")
    public String filterPublisherResult(@RequestParam("pubId") String pubId, Model model) {
        model.addAttribute("employees", employeeClient.filterByPublisher(pubId));
        model.addAttribute("pageTitle", "Employees Working for Publisher: " + pubId);
        return "employees/employee-list";
    }

    // --- RELATIONAL (HOPS) ---
    @GetMapping("/relational/job")
    public String relJob(Model model) {
        model.addAttribute("formTitle", "Get Job Details (1-Hop)");
        model.addAttribute("actionUrl", "/web/employees/relational/job/result");
        return "employees/employee-id-request";
    }

    @GetMapping("/relational/job/result")
    public String relJobResult(@RequestParam("id") String id, Model model) {
        model.addAttribute("targetId", id);
        model.addAttribute("job", employeeClient.getEmployeeJob(id));
        return "employees/employee-job";
    }

    @GetMapping("/relational/publisher")
    public String relPublisher(Model model) {
        model.addAttribute("formTitle", "Get Publisher Details (1-Hop)");
        model.addAttribute("actionUrl", "/web/employees/relational/publisher/result");
        return "employees/employee-id-request";
    }

    @GetMapping("/relational/publisher/result")
    public String relPublisherResult(@RequestParam("id") String id, Model model) {
        model.addAttribute("targetId", id);
        model.addAttribute("publisher", employeeClient.getEmployeePublisher(id));
        return "employees/employee-publisher";
    }

    @GetMapping("/relational/publisher-titles")
    public String relPublisherTitles(Model model) {
        model.addAttribute("formTitle", "Get Publisher's Titles (2-Hop)");
        model.addAttribute("actionUrl", "/web/employees/relational/publisher-titles/result");
        return "employees/employee-id-request";
    }

    @GetMapping("/relational/publisher-titles/result")
    public String relPublisherTitlesResult(@RequestParam("id") String id, Model model) {
        model.addAttribute("targetId", id);
        model.addAttribute("titles", employeeClient.getEmployeePublisherTitles(id));
        return "employees/employee-publisher-titles";
    }
}