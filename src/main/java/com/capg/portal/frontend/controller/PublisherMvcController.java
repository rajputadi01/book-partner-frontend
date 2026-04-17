package com.capg.portal.frontend.controller;

import com.capg.portal.frontend.client.PublisherClient;
import com.capg.portal.frontend.dto.PublisherDto;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/web/publishers")
public class PublisherMvcController {

    private final PublisherClient publisherClient;

    public PublisherMvcController(PublisherClient publisherClient) {
        this.publisherClient = publisherClient;
    }

    @GetMapping("/operations")
    public String showOperationsMenu() {
        return "publishers/publisher-operations";
    }

    @GetMapping("/get-all")
    public String getAll(Model model) {
        model.addAttribute("publishers", publisherClient.getAllPublishers());
        model.addAttribute("pageTitle", "All Publisher Records");
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
        publisherClient.createPublisher(pub);
        return "redirect:/web/publishers/get-all";
    }

    @GetMapping("/get-by-id")
    public String showIdRequest(Model model) {
        model.addAttribute("formTitle", "Get Publisher by ID");
        model.addAttribute("actionUrl", "/web/publishers/get-by-id/result");
        return "publishers/publisher-id-request";
    }

    @GetMapping("/get-by-id/result")
    public String getByIdResult(@RequestParam("id") String id, Model model) {
        model.addAttribute("publisher", publisherClient.getPublisherById(id));
        return "publishers/publisher-details";
    }

    @GetMapping("/update")
    public String showUpdateIdRequest(Model model) {
        model.addAttribute("formTitle", "Update Publisher (PUT)");
        model.addAttribute("actionUrl", "/web/publishers/update/form");
        return "publishers/publisher-id-request";
    }

    @GetMapping("/update/form")
    public String showUpdateForm(@RequestParam("id") String id, Model model) {
        model.addAttribute("publisher", publisherClient.getPublisherById(id));
        model.addAttribute("formTitle", "Update Publisher Data");
        model.addAttribute("actionUrl", "/web/publishers/update/save");
        model.addAttribute("isUpdate", true);
        return "publishers/publisher-form";
    }

    @PostMapping("/update/save")
    public String saveUpdate(@Valid @ModelAttribute("publisher") PublisherDto pub, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("formTitle", "Update Publisher Data");
            model.addAttribute("actionUrl", "/web/publishers/update/save");
            model.addAttribute("isUpdate", true);
            return "publishers/publisher-form";
        }
        publisherClient.updatePublisher(pub.getPubId(), pub);
        return "redirect:/web/publishers/get-all";
    }

    @GetMapping("/patch")
    public String showPatchIdRequest(Model model) {
        model.addAttribute("formTitle", "Patch Publisher");
        model.addAttribute("actionUrl", "/web/publishers/patch/form");
        return "publishers/publisher-id-request";
    }

    @GetMapping("/patch/form")
    public String showPatchForm(@RequestParam("id") String id, Model model) {
        model.addAttribute("publisher", publisherClient.getPublisherById(id));
        model.addAttribute("formTitle", "Patch Publisher Data");
        model.addAttribute("actionUrl", "/web/publishers/patch/save");
        model.addAttribute("isUpdate", true);
        return "publishers/publisher-form";
    }

    @PostMapping("/patch/save")
    public String savePatch(@ModelAttribute("publisher") PublisherDto pub) {
        publisherClient.patchPublisher(pub.getPubId(), pub);
        return "redirect:/web/publishers/get-all";
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
    public String filterCityResult(@RequestParam("city") String city, Model model) {
        model.addAttribute("publishers", publisherClient.getPublishersByCity(city));
        model.addAttribute("pageTitle", "Publishers in City: " + city);
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
    public String filterStateResult(@RequestParam("state") String state, Model model) {
        model.addAttribute("publishers", publisherClient.getPublishersByState(state));
        model.addAttribute("pageTitle", "Publishers in State: " + state);
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
    public String filterCountryResult(@RequestParam("country") String country, Model model) {
        model.addAttribute("publishers", publisherClient.getPublishersByCountry(country));
        model.addAttribute("pageTitle", "Publishers in Country: " + country);
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
    public String relEmpResult(@RequestParam("id") String id, Model model) {
        model.addAttribute("targetId", id);
        model.addAttribute("employees", publisherClient.getEmployeesByPublisher(id));
        return "publishers/publisher-employees";
    }

    @GetMapping("/relational/titles")
    public String relTitles(Model model) {
        model.addAttribute("formTitle", "Get Titles by Publisher");
        model.addAttribute("actionUrl", "/web/publishers/relational/titles/result");
        return "publishers/publisher-id-request";
    }

    @GetMapping("/relational/titles/result")
    public String relTitlesResult(@RequestParam("id") String id, Model model) {
        model.addAttribute("targetId", id);
        model.addAttribute("titles", publisherClient.getTitlesByPublisher(id));
        return "publishers/publisher-titles";
    }

    @GetMapping("/relational/authors")
    public String relAuthors(Model model) {
        model.addAttribute("formTitle", "Get Authors by Publisher");
        model.addAttribute("actionUrl", "/web/publishers/relational/authors/result");
        return "publishers/publisher-id-request";
    }

    @GetMapping("/relational/authors/result")
    public String relAuthorsResult(@RequestParam("id") String id, Model model) {
        model.addAttribute("targetId", id);
        model.addAttribute("authors", publisherClient.getAuthorsByPublisher(id));
        return "publishers/publisher-authors";
    }

    @GetMapping("/relational/stores")
    public String relStores(Model model) {
        model.addAttribute("formTitle", "Get Stores by Publisher");
        model.addAttribute("actionUrl", "/web/publishers/relational/stores/result");
        return "publishers/publisher-id-request";
    }

    @GetMapping("/relational/stores/result")
    public String relStoresResult(@RequestParam("id") String id, Model model) {
        model.addAttribute("targetId", id);
        model.addAttribute("stores", publisherClient.getStoresByPublisher(id));
        return "publishers/publisher-stores";
    }
}