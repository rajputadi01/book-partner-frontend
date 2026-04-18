package com.capg.portal.frontend.controller;

import com.capg.portal.frontend.client.PublisherClient;
import com.capg.portal.frontend.dto.*;
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
@RequestMapping("/web/publishers")
public class PublisherMvcController {

    private final PublisherClient publisherClient;
    private final int PAGE_SIZE = 5; // Standard pagination size

    public PublisherMvcController(PublisherClient publisherClient) {
        this.publisherClient = publisherClient;
    }

    @GetMapping("/operations")
    public String showOperationsMenu() {
        return "publishers/publisher-operations";
    }

    // --- CRUD ---
    @GetMapping("/get-all")
    public String getAll(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "sortBy", defaultValue = "pubId") String sortBy,
            @RequestParam(value = "dir", defaultValue = "asc") String dir,
            Model model) {
        
        List<PublisherDto> list = publisherClient.getAllPublishers();
        sortPublishers(list, sortBy, dir);

        model.addAttribute("publishers", paginateList(list, page, PAGE_SIZE, model));
        model.addAttribute("pageTitle", "All Publisher Records");
        model.addAttribute("endpoint", "/web/publishers/get-all");
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("dir", dir);
        return "publishers/publisher-list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("publisher", new PublisherDto());
        model.addAttribute("formTitle", "Create New Publisher");
        model.addAttribute("actionUrl", "/web/publishers/create/save");
        model.addAttribute("isUpdate", false);
        return "publishers/publisher-form";
    }

    @PostMapping("/create/save")
    public String saveCreate(@Valid @ModelAttribute("publisher") PublisherDto pub, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("formTitle", "Create New Publisher");
            model.addAttribute("actionUrl", "/web/publishers/create/save");
            model.addAttribute("isUpdate", false);
            return "publishers/publisher-form";
        }
        try {
            publisherClient.createPublisher(pub);
            return "redirect:/web/publishers/get-all";
        } catch (HttpStatusCodeException e) {
            model.addAttribute("errorMessage", extractErrorMessage(e));
            model.addAttribute("formTitle", "Create New Publisher");
            model.addAttribute("actionUrl", "/web/publishers/create/save");
            model.addAttribute("isUpdate", false);
            return "publishers/publisher-form";
        }
    }

    @GetMapping("/get-by-id")
    public String showIdRequest(Model model) {
        model.addAttribute("formTitle", "Get Publisher by ID");
        model.addAttribute("actionUrl", "/web/publishers/get-by-id/result");
        return "publishers/publisher-id-request";
    }

    @GetMapping("/get-by-id/result")
    public String getByIdResult(@RequestParam("id") String id, Model model, RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("publisher", publisherClient.getPublisherById(id));
            return "publishers/publisher-details";
        } catch (HttpStatusCodeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", extractErrorMessage(e));
            return "redirect:/web/publishers/get-by-id";
        }
    }

    @GetMapping("/update")
    public String showUpdateIdRequest(Model model) {
        model.addAttribute("formTitle", "Update Publisher (PUT)");
        model.addAttribute("actionUrl", "/web/publishers/update/form");
        return "publishers/publisher-id-request";
    }

    @GetMapping("/update/form")
    public String showUpdateForm(@RequestParam("id") String id, Model model, RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("publisher", publisherClient.getPublisherById(id));
            model.addAttribute("formTitle", "Update Publisher Data");
            model.addAttribute("actionUrl", "/web/publishers/update/save");
            model.addAttribute("isUpdate", true);
            return "publishers/publisher-form";
        } catch (HttpStatusCodeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", extractErrorMessage(e));
            return "redirect:/web/publishers/update";
        }
    }

    @PostMapping("/update/save")
    public String saveUpdate(@Valid @ModelAttribute("publisher") PublisherDto pub, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("formTitle", "Update Publisher Data");
            model.addAttribute("actionUrl", "/web/publishers/update/save");
            model.addAttribute("isUpdate", true);
            return "publishers/publisher-form";
        }
        try {
            publisherClient.updatePublisher(pub.getPubId(), pub);
            return "redirect:/web/publishers/get-all";
        } catch (HttpStatusCodeException e) {
            model.addAttribute("errorMessage", extractErrorMessage(e));
            model.addAttribute("formTitle", "Update Publisher Data");
            model.addAttribute("actionUrl", "/web/publishers/update/save");
            model.addAttribute("isUpdate", true);
            return "publishers/publisher-form";
        }
    }

    @GetMapping("/patch")
    public String showPatchIdRequest(Model model) {
        model.addAttribute("formTitle", "Patch Publisher");
        model.addAttribute("actionUrl", "/web/publishers/patch/form");
        return "publishers/publisher-id-request";
    }

    @GetMapping("/patch/form")
    public String showPatchForm(@RequestParam("id") String id, Model model, RedirectAttributes redirectAttributes) {
        try {
            // 1. Current data for read-only display
            model.addAttribute("currentPublisher", publisherClient.getPublisherById(id));
            
            // 2. Blank DTO for patching
            PublisherDto patchDto = new PublisherDto();
            patchDto.setPubId(id);
            
            model.addAttribute("publisher", patchDto);
            model.addAttribute("formTitle", "Patch Publisher Data");
            model.addAttribute("actionUrl", "/web/publishers/patch/save");
            return "publishers/publisher-patch";
        } catch (HttpStatusCodeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", extractErrorMessage(e));
            return "redirect:/web/publishers/patch";
        }
    }

    @PostMapping("/patch/save")
    public String savePatch(@ModelAttribute("publisher") PublisherDto pub, RedirectAttributes redirectAttributes) {
        // Sanitization: convert empty strings to null so backend ignores them
        if (pub.getPubName() != null && pub.getPubName().trim().isEmpty()) pub.setPubName(null);
        if (pub.getCity() != null && pub.getCity().trim().isEmpty()) pub.setCity(null);
        if (pub.getState() != null && pub.getState().trim().isEmpty()) pub.setState(null);
        if (pub.getCountry() != null && pub.getCountry().trim().isEmpty()) pub.setCountry(null);

        try {
            publisherClient.patchPublisher(pub.getPubId(), pub);
            return "redirect:/web/publishers/get-all";
        } catch (HttpStatusCodeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", extractErrorMessage(e));
            return "redirect:/web/publishers/patch/form?id=" + pub.getPubId();
        }
    }

    // --- FILTERS ---
    @GetMapping("/filter/city")
    public String filterCity(Model model) {
        model.addAttribute("formTitle", "Filter by City");
        model.addAttribute("actionUrl", "/web/publishers/filter/city/result");
        model.addAttribute("paramName", "city");
        return "publishers/publisher-string-request";
    }

    @GetMapping("/filter/city/result")
    public String filterCityResult(
            @RequestParam("city") String city,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "sortBy", defaultValue = "pubId") String sortBy,
            @RequestParam(value = "dir", defaultValue = "asc") String dir,
            Model model) {
        
        List<PublisherDto> list = publisherClient.getPublishersByCity(city);
        sortPublishers(list, sortBy, dir);

        model.addAttribute("publishers", paginateList(list, page, PAGE_SIZE, model));
        model.addAttribute("pageTitle", "Publishers in City: " + city);
        model.addAttribute("endpoint", "/web/publishers/filter/city/result");
        model.addAttribute("city", city);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("dir", dir);
        return "publishers/publisher-list";
    }

    @GetMapping("/filter/state")
    public String filterState(Model model) {
        model.addAttribute("formTitle", "Filter by State");
        model.addAttribute("actionUrl", "/web/publishers/filter/state/result");
        model.addAttribute("paramName", "state");
        return "publishers/publisher-string-request";
    }

    @GetMapping("/filter/state/result")
    public String filterStateResult(
            @RequestParam("state") String state,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "sortBy", defaultValue = "pubId") String sortBy,
            @RequestParam(value = "dir", defaultValue = "asc") String dir,
            Model model) {
        
        List<PublisherDto> list = publisherClient.getPublishersByState(state);
        sortPublishers(list, sortBy, dir);

        model.addAttribute("publishers", paginateList(list, page, PAGE_SIZE, model));
        model.addAttribute("pageTitle", "Publishers in State: " + state);
        model.addAttribute("endpoint", "/web/publishers/filter/state/result");
        model.addAttribute("state", state);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("dir", dir);
        return "publishers/publisher-list";
    }

    @GetMapping("/filter/country")
    public String filterCountry(Model model) {
        model.addAttribute("formTitle", "Filter by Country");
        model.addAttribute("actionUrl", "/web/publishers/filter/country/result");
        model.addAttribute("paramName", "country");
        return "publishers/publisher-string-request";
    }

    @GetMapping("/filter/country/result")
    public String filterCountryResult(
            @RequestParam("country") String country,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "sortBy", defaultValue = "pubId") String sortBy,
            @RequestParam(value = "dir", defaultValue = "asc") String dir,
            Model model) {
        
        List<PublisherDto> list = publisherClient.getPublishersByCountry(country);
        sortPublishers(list, sortBy, dir);

        model.addAttribute("publishers", paginateList(list, page, PAGE_SIZE, model));
        model.addAttribute("pageTitle", "Publishers in Country: " + country);
        model.addAttribute("endpoint", "/web/publishers/filter/country/result");
        model.addAttribute("country", country);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("dir", dir);
        return "publishers/publisher-list";
    }

    // --- RELATIONAL ---
    @GetMapping("/relational/employees")
    public String relEmp(Model model) {
        model.addAttribute("formTitle", "Get Employees by Publisher");
        model.addAttribute("actionUrl", "/web/publishers/relational/employees/result");
        return "publishers/publisher-id-request";
    }

    @GetMapping("/relational/employees/result")
    public String relEmpResult(
            @RequestParam("id") String id,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "sortBy", defaultValue = "empId") String sortBy,
            @RequestParam(value = "dir", defaultValue = "asc") String dir,
            Model model, RedirectAttributes redirectAttributes) {
        try {
            publisherClient.getPublisherById(id); // Validate publisher exists
            List<EmployeeDto> list = publisherClient.getEmployeesByPublisher(id);
            sortEmployees(list, sortBy, dir);

            model.addAttribute("targetId", id);
            model.addAttribute("employees", paginateList(list, page, PAGE_SIZE, model));
            model.addAttribute("endpoint", "/web/publishers/relational/employees/result");
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("dir", dir);
            return "publishers/publisher-employees";
        } catch (HttpStatusCodeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", extractErrorMessage(e));
            return "redirect:/web/publishers/relational/employees";
        }
    }

    @GetMapping("/relational/titles")
    public String relTitles(Model model) {
        model.addAttribute("formTitle", "Get Titles by Publisher");
        model.addAttribute("actionUrl", "/web/publishers/relational/titles/result");
        return "publishers/publisher-id-request";
    }

    @GetMapping("/relational/titles/result")
    public String relTitlesResult(
            @RequestParam("id") String id,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "sortBy", defaultValue = "titleId") String sortBy,
            @RequestParam(value = "dir", defaultValue = "asc") String dir,
            Model model, RedirectAttributes redirectAttributes) {
        try {
            publisherClient.getPublisherById(id); // Validate publisher exists
            List<TitleDto> list = publisherClient.getTitlesByPublisher(id);
            sortTitles(list, sortBy, dir);

            model.addAttribute("targetId", id);
            model.addAttribute("titles", paginateList(list, page, PAGE_SIZE, model));
            model.addAttribute("endpoint", "/web/publishers/relational/titles/result");
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("dir", dir);
            return "publishers/publisher-titles";
        } catch (HttpStatusCodeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", extractErrorMessage(e));
            return "redirect:/web/publishers/relational/titles";
        }
    }

    @GetMapping("/relational/authors")
    public String relAuthors(Model model) {
        model.addAttribute("formTitle", "Get Authors by Publisher");
        model.addAttribute("actionUrl", "/web/publishers/relational/authors/result");
        return "publishers/publisher-id-request";
    }

    @GetMapping("/relational/authors/result")
    public String relAuthorsResult(
            @RequestParam("id") String id,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "sortBy", defaultValue = "auId") String sortBy,
            @RequestParam(value = "dir", defaultValue = "asc") String dir,
            Model model, RedirectAttributes redirectAttributes) {
        try {
            publisherClient.getPublisherById(id); // Validate publisher exists
            List<AuthorDto> list = publisherClient.getAuthorsByPublisher(id);
            sortAuthors(list, sortBy, dir);

            model.addAttribute("targetId", id);
            model.addAttribute("authors", paginateList(list, page, PAGE_SIZE, model));
            model.addAttribute("endpoint", "/web/publishers/relational/authors/result");
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("dir", dir);
            return "publishers/publisher-authors";
        } catch (HttpStatusCodeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", extractErrorMessage(e));
            return "redirect:/web/publishers/relational/authors";
        }
    }

    @GetMapping("/relational/stores")
    public String relStores(Model model) {
        model.addAttribute("formTitle", "Get Stores by Publisher");
        model.addAttribute("actionUrl", "/web/publishers/relational/stores/result");
        return "publishers/publisher-id-request";
    }

    @GetMapping("/relational/stores/result")
    public String relStoresResult(
            @RequestParam("id") String id,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "sortBy", defaultValue = "storId") String sortBy,
            @RequestParam(value = "dir", defaultValue = "asc") String dir,
            Model model, RedirectAttributes redirectAttributes) {
        try {
            publisherClient.getPublisherById(id); // Validate publisher exists
            List<StoreDto> list = publisherClient.getStoresByPublisher(id);
            sortStores(list, sortBy, dir);

            model.addAttribute("targetId", id);
            model.addAttribute("stores", paginateList(list, page, PAGE_SIZE, model));
            model.addAttribute("endpoint", "/web/publishers/relational/stores/result");
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("dir", dir);
            return "publishers/publisher-stores";
        } catch (HttpStatusCodeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", extractErrorMessage(e));
            return "redirect:/web/publishers/relational/stores";
        }
    }

    // ==========================================
    // UTILITY ENGINES
    // ==========================================

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

    private void sortPublishers(List<PublisherDto> list, String sortBy, String dir) {
        if ("pubName".equals(sortBy)) list.sort(Comparator.comparing(PublisherDto::getPubName, Comparator.nullsLast(String::compareToIgnoreCase)));
        else if ("city".equals(sortBy)) list.sort(Comparator.comparing(PublisherDto::getCity, Comparator.nullsLast(String::compareToIgnoreCase)));
        else if ("state".equals(sortBy)) list.sort(Comparator.comparing(PublisherDto::getState, Comparator.nullsLast(String::compareToIgnoreCase)));
        else if ("country".equals(sortBy)) list.sort(Comparator.comparing(PublisherDto::getCountry, Comparator.nullsLast(String::compareToIgnoreCase)));
        else list.sort(Comparator.comparing(PublisherDto::getPubId));
        if ("desc".equals(dir)) Collections.reverse(list);
    }

    private void sortEmployees(List<EmployeeDto> list, String sortBy, String dir) {
        if ("fname".equals(sortBy)) list.sort(Comparator.comparing(EmployeeDto::getFname, Comparator.nullsLast(String::compareToIgnoreCase)));
        else if ("lname".equals(sortBy)) list.sort(Comparator.comparing(EmployeeDto::getLname, Comparator.nullsLast(String::compareToIgnoreCase)));
        else if ("jobLvl".equals(sortBy)) list.sort(Comparator.comparing(EmployeeDto::getJobLvl, Comparator.nullsLast(Comparator.naturalOrder())));
        else list.sort(Comparator.comparing(EmployeeDto::getEmpId));
        if ("desc".equals(dir)) Collections.reverse(list);
    }

    private void sortTitles(List<TitleDto> list, String sortBy, String dir) {
        if ("titleName".equals(sortBy)) list.sort(Comparator.comparing(TitleDto::getTitleName, Comparator.nullsLast(String::compareToIgnoreCase)));
        else if ("type".equals(sortBy)) list.sort(Comparator.comparing(TitleDto::getType, Comparator.nullsLast(String::compareToIgnoreCase)));
        else if ("price".equals(sortBy)) list.sort(Comparator.comparing(TitleDto::getPrice, Comparator.nullsLast(Comparator.naturalOrder())));
        else list.sort(Comparator.comparing(TitleDto::getTitleId));
        if ("desc".equals(dir)) Collections.reverse(list);
    }

    private void sortAuthors(List<AuthorDto> list, String sortBy, String dir) {
        if ("auFname".equals(sortBy)) list.sort(Comparator.comparing(AuthorDto::getAuFname, Comparator.nullsLast(String::compareToIgnoreCase)));
        else if ("auLname".equals(sortBy)) list.sort(Comparator.comparing(AuthorDto::getAuLname, Comparator.nullsLast(String::compareToIgnoreCase)));
        else if ("phone".equals(sortBy)) list.sort(Comparator.comparing(AuthorDto::getPhone, Comparator.nullsLast(String::compareToIgnoreCase)));
        else list.sort(Comparator.comparing(AuthorDto::getAuId));
        if ("desc".equals(dir)) Collections.reverse(list);
    }

    private void sortStores(List<StoreDto> list, String sortBy, String dir) {
        if ("storName".equals(sortBy)) list.sort(Comparator.comparing(StoreDto::getStorName, Comparator.nullsLast(String::compareToIgnoreCase)));
        else if ("city".equals(sortBy)) list.sort(Comparator.comparing(StoreDto::getCity, Comparator.nullsLast(String::compareToIgnoreCase)));
        else if ("state".equals(sortBy)) list.sort(Comparator.comparing(StoreDto::getState, Comparator.nullsLast(String::compareToIgnoreCase)));
        else list.sort(Comparator.comparing(StoreDto::getStorId));
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