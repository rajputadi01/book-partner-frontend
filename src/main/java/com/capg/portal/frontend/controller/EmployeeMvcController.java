package com.capg.portal.frontend.controller;

import com.capg.portal.frontend.client.EmployeeClient;
import com.capg.portal.frontend.dto.EmployeeDto;
import com.capg.portal.frontend.dto.JobDto;
import com.capg.portal.frontend.dto.PublisherDto;
import com.capg.portal.frontend.dto.TitleDto;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Controller
@RequestMapping("/web/employees")
public class EmployeeMvcController {

    private final EmployeeClient employeeClient;
    private final int PAGE_SIZE = 5;

    public EmployeeMvcController(EmployeeClient employeeClient) {
        this.employeeClient = employeeClient;
    }

    @GetMapping("/operations")
    public String showOperationsMenu() {
        return "employees/employee-operations";
    }

    // --- CRUD ---
    @GetMapping("/get-all")
    public String getAll(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "sortBy", defaultValue = "empId") String sortBy,
            @RequestParam(value = "dir", defaultValue = "asc") String dir,
            Model model) {
        
        List<EmployeeDto> list = employeeClient.getAllEmployees();
        sortEmployees(list, sortBy, dir);

        model.addAttribute("employees", paginateList(list, page, PAGE_SIZE, model));
        model.addAttribute("pageTitle", "All Employee Records");
        model.addAttribute("endpoint", "/web/employees/get-all");
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("dir", dir);
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
        
        cleanNestedObjects(employee);
        
        try {
            employeeClient.createEmployee(employee);
            return "redirect:/web/employees/get-all";
        } catch (HttpStatusCodeException e) {
            model.addAttribute("errorMessage", extractErrorMessage(e));
            model.addAttribute("formTitle", "Register New Employee");
            model.addAttribute("actionUrl", "/web/employees/create/save");
            model.addAttribute("isUpdate", false);
            return "employees/employee-form";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "An unexpected error occurred: " + e.getMessage());
            model.addAttribute("formTitle", "Register New Employee");
            model.addAttribute("actionUrl", "/web/employees/create/save");
            model.addAttribute("isUpdate", false);
            return "employees/employee-form";
        }
    }

    @GetMapping("/get-by-id")
    public String showIdRequest(Model model) {
        model.addAttribute("formTitle", "Fetch Employee by ID");
        model.addAttribute("actionUrl", "/web/employees/get-by-id/result");
        return "employees/employee-id-request";
    }

    @GetMapping("/get-by-id/result")
    public String getIdResult(@RequestParam("id") String id, Model model, RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("employee", employeeClient.getEmployeeById(id));
            return "employees/employee-details";
        } catch (HttpStatusCodeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", extractErrorMessage(e));
            return "redirect:/web/employees/get-by-id";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred: " + e.getMessage());
            return "redirect:/web/employees/get-by-id";
        }
    }

    @GetMapping("/update")
    public String showUpdateIdRequest(Model model) {
        model.addAttribute("formTitle", "Update Employee (PUT)");
        model.addAttribute("actionUrl", "/web/employees/update/form");
        return "employees/employee-id-request";
    }

    @GetMapping("/update/form")
    public String showUpdateForm(@RequestParam("id") String id, Model model, RedirectAttributes redirectAttributes) {
        try {
            EmployeeDto employee = employeeClient.getEmployeeById(id);
            if (employee.getJob() == null) employee.setJob(new JobDto());
            if (employee.getPublisher() == null) employee.setPublisher(new PublisherDto());

            model.addAttribute("employee", employee);
            model.addAttribute("formTitle", "Update Employee Data");
            model.addAttribute("actionUrl", "/web/employees/update/save");
            model.addAttribute("isUpdate", true);
            return "employees/employee-form";
        } catch (HttpStatusCodeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", extractErrorMessage(e));
            return "redirect:/web/employees/update";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred: " + e.getMessage());
            return "redirect:/web/employees/update";
        }
    }

    @PostMapping("/update/save")
    public String saveUpdate(@Valid @ModelAttribute("employee") EmployeeDto employee, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("formTitle", "Update Employee Data");
            model.addAttribute("actionUrl", "/web/employees/update/save");
            model.addAttribute("isUpdate", true);
            return "employees/employee-form";
        }
        
        cleanNestedObjects(employee);

        try {
            employeeClient.updateEmployee(employee.getEmpId(), employee);
            return "redirect:/web/employees/get-all";
        } catch (HttpStatusCodeException e) {
            model.addAttribute("errorMessage", extractErrorMessage(e));
            model.addAttribute("formTitle", "Update Employee Data");
            model.addAttribute("actionUrl", "/web/employees/update/save");
            model.addAttribute("isUpdate", true);
            return "employees/employee-form";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "An unexpected error occurred: " + e.getMessage());
            model.addAttribute("formTitle", "Update Employee Data");
            model.addAttribute("actionUrl", "/web/employees/update/save");
            model.addAttribute("isUpdate", true);
            return "employees/employee-form";
        }
    }

    @GetMapping("/patch")
    public String showPatchIdRequest(Model model) {
        model.addAttribute("formTitle", "Patch Employee");
        model.addAttribute("actionUrl", "/web/employees/patch/form");
        return "employees/employee-id-request";
    }

    @GetMapping("/patch/form")
    public String showPatchForm(@RequestParam("id") String id, Model model, RedirectAttributes redirectAttributes) {
        try {
            // Current Data for Display
            model.addAttribute("currentRecord", employeeClient.getEmployeeById(id));
            
            // Blank Form DTO
            EmployeeDto patchDto = new EmployeeDto();
            patchDto.setEmpId(id);
            patchDto.setJob(new JobDto());
            patchDto.setPublisher(new PublisherDto());

            model.addAttribute("employee", patchDto);
            model.addAttribute("formTitle", "Patch Employee Data");
            model.addAttribute("actionUrl", "/web/employees/patch/save");
            return "employees/employee-patch";
        } catch (HttpStatusCodeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", extractErrorMessage(e));
            return "redirect:/web/employees/patch";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred: " + e.getMessage());
            return "redirect:/web/employees/patch";
        }
    }

    @PostMapping("/patch/save")
    public String savePatch(@ModelAttribute("employee") EmployeeDto employee, RedirectAttributes redirectAttributes) {
        // Sanitization
        if (employee.getFname() != null && employee.getFname().trim().isEmpty()) employee.setFname(null);
        if (employee.getMinit() != null && employee.getMinit().trim().isEmpty()) employee.setMinit(null);
        if (employee.getLname() != null && employee.getLname().trim().isEmpty()) employee.setLname(null);
        
        cleanNestedObjects(employee);

        try {
            employeeClient.patchEmployee(employee.getEmpId(), employee);
            return "redirect:/web/employees/get-all";
        } catch (HttpStatusCodeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", extractErrorMessage(e));
            return "redirect:/web/employees/patch/form?id=" + employee.getEmpId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred: " + e.getMessage());
            return "redirect:/web/employees/patch/form?id=" + employee.getEmpId();
        }
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
    public String filterJobResult(
            @RequestParam("jobId") Short jobId, 
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "sortBy", defaultValue = "empId") String sortBy,
            @RequestParam(value = "dir", defaultValue = "asc") String dir,
            Model model, RedirectAttributes redirectAttributes) {
        try {
            List<EmployeeDto> list = employeeClient.filterByJob(jobId);
            sortEmployees(list, sortBy, dir);

            model.addAttribute("employees", paginateList(list, page, PAGE_SIZE, model));
            model.addAttribute("pageTitle", "Employees Assigned to Job ID: " + jobId);
            model.addAttribute("endpoint", "/web/employees/filter/job/result");
            model.addAttribute("jobId", jobId);
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("dir", dir);
            return "employees/employee-list";
        } catch (HttpStatusCodeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", extractErrorMessage(e));
            return "redirect:/web/employees/filter/job";
        }
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
    public String filterJobLevelResult(
            @RequestParam("maxLvl") Integer maxLvl,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "sortBy", defaultValue = "empId") String sortBy,
            @RequestParam(value = "dir", defaultValue = "asc") String dir,
            Model model, RedirectAttributes redirectAttributes) {
        try {
            List<EmployeeDto> list = employeeClient.filterByJobLevel(maxLvl);
            sortEmployees(list, sortBy, dir);

            model.addAttribute("employees", paginateList(list, page, PAGE_SIZE, model));
            model.addAttribute("pageTitle", "Employees with Job Level < " + maxLvl);
            model.addAttribute("endpoint", "/web/employees/filter/job-level/result");
            model.addAttribute("maxLvl", maxLvl);
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("dir", dir);
            return "employees/employee-list";
        } catch (HttpStatusCodeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", extractErrorMessage(e));
            return "redirect:/web/employees/filter/job-level";
        }
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
    public String filterPublisherResult(
            @RequestParam("pubId") String pubId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "sortBy", defaultValue = "empId") String sortBy,
            @RequestParam(value = "dir", defaultValue = "asc") String dir,
            Model model, RedirectAttributes redirectAttributes) {
        try {
            List<EmployeeDto> list = employeeClient.filterByPublisher(pubId);
            sortEmployees(list, sortBy, dir);

            model.addAttribute("employees", paginateList(list, page, PAGE_SIZE, model));
            model.addAttribute("pageTitle", "Employees Working for Publisher: " + pubId);
            model.addAttribute("endpoint", "/web/employees/filter/publisher/result");
            model.addAttribute("pubId", pubId);
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("dir", dir);
            return "employees/employee-list";
        } catch (HttpStatusCodeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", extractErrorMessage(e));
            return "redirect:/web/employees/filter/publisher";
        }
    }

    // --- RELATIONAL (HOPS) ---
    @GetMapping("/relational/job")
    public String relJob(Model model) {
        model.addAttribute("formTitle", "Get Job Details (1-Hop)");
        model.addAttribute("actionUrl", "/web/employees/relational/job/result");
        return "employees/employee-id-request";
    }

    @GetMapping("/relational/job/result")
    public String relJobResult(@RequestParam("id") String id, Model model, RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("targetId", id);
            model.addAttribute("job", employeeClient.getEmployeeJob(id));
            return "employees/employee-job";
        } catch (HttpStatusCodeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", extractErrorMessage(e));
            return "redirect:/web/employees/relational/job";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred: " + e.getMessage());
            return "redirect:/web/employees/relational/job";
        }
    }

    @GetMapping("/relational/publisher")
    public String relPublisher(Model model) {
        model.addAttribute("formTitle", "Get Publisher Details (1-Hop)");
        model.addAttribute("actionUrl", "/web/employees/relational/publisher/result");
        return "employees/employee-id-request";
    }

    @GetMapping("/relational/publisher/result")
    public String relPublisherResult(@RequestParam("id") String id, Model model, RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("targetId", id);
            model.addAttribute("publisher", employeeClient.getEmployeePublisher(id));
            return "employees/employee-publisher";
        } catch (HttpStatusCodeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", extractErrorMessage(e));
            return "redirect:/web/employees/relational/publisher";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred: " + e.getMessage());
            return "redirect:/web/employees/relational/publisher";
        }
    }

    @GetMapping("/relational/publisher-titles")
    public String relPublisherTitles(Model model) {
        model.addAttribute("formTitle", "Get Publisher's Titles (2-Hop)");
        model.addAttribute("actionUrl", "/web/employees/relational/publisher-titles/result");
        return "employees/employee-id-request";
    }

    @GetMapping("/relational/publisher-titles/result")
    public String relPublisherTitlesResult(
            @RequestParam("id") String id,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "sortBy", defaultValue = "titleId") String sortBy,
            @RequestParam(value = "dir", defaultValue = "asc") String dir,
            Model model, RedirectAttributes redirectAttributes) {
        try {
            List<TitleDto> list = employeeClient.getEmployeePublisherTitles(id);
            sortTitles(list, sortBy, dir);

            model.addAttribute("targetId", id);
            model.addAttribute("titles", paginateList(list, page, PAGE_SIZE, model));
            model.addAttribute("endpoint", "/web/employees/relational/publisher-titles/result");
            model.addAttribute("id", id);
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("dir", dir);
            return "employees/employee-publisher-titles";
        } catch (HttpStatusCodeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", extractErrorMessage(e));
            return "redirect:/web/employees/relational/publisher-titles";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred: " + e.getMessage());
            return "redirect:/web/employees/relational/publisher-titles";
        }
    }

    // ==========================================
    // UTILITY ENGINES
    // ==========================================

    private void cleanNestedObjects(EmployeeDto employee) {
        if (employee.getJob() != null && employee.getJob().getJobId() == null) {
            employee.setJob(null);
        }
        if (employee.getPublisher() != null && (employee.getPublisher().getPubId() == null || employee.getPublisher().getPubId().trim().isEmpty())) {
            employee.setPublisher(null);
        }
    }

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

    private void sortEmployees(List<EmployeeDto> list, String sortBy, String dir) {
        if ("fname".equals(sortBy)) list.sort(Comparator.comparing(EmployeeDto::getFname, Comparator.nullsLast(String::compareToIgnoreCase)));
        else if ("lname".equals(sortBy)) list.sort(Comparator.comparing(EmployeeDto::getLname, Comparator.nullsLast(String::compareToIgnoreCase)));
        else if ("jobId".equals(sortBy)) list.sort(Comparator.comparing(e -> e.getJob() != null ? e.getJob().getJobId() : 0, Comparator.nullsLast(Comparator.naturalOrder())));
        else if ("jobLvl".equals(sortBy)) list.sort(Comparator.comparing(EmployeeDto::getJobLvl, Comparator.nullsLast(Comparator.naturalOrder())));
        else if ("pubId".equals(sortBy)) list.sort(Comparator.comparing(e -> e.getPublisher() != null ? e.getPublisher().getPubId() : "", Comparator.nullsLast(String::compareToIgnoreCase)));
        else list.sort(Comparator.comparing(EmployeeDto::getEmpId, Comparator.nullsLast(String::compareToIgnoreCase)));
        
        if ("desc".equals(dir)) Collections.reverse(list);
    }

    private void sortTitles(List<TitleDto> list, String sortBy, String dir) {
        if ("titleName".equals(sortBy)) list.sort(Comparator.comparing(TitleDto::getTitleName, Comparator.nullsLast(String::compareToIgnoreCase)));
        else if ("type".equals(sortBy)) list.sort(Comparator.comparing(TitleDto::getType, Comparator.nullsLast(String::compareToIgnoreCase)));
        else if ("price".equals(sortBy)) list.sort(Comparator.comparing(TitleDto::getPrice, Comparator.nullsLast(Comparator.naturalOrder())));
        else list.sort(Comparator.comparing(TitleDto::getTitleId));
        
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
}