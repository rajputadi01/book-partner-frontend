package com.capg.portal.frontend.controller;

import com.capg.portal.frontend.client.AuthorClient;
import com.capg.portal.frontend.dto.AuthorDto;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/web/authors")
public class AuthorMvcController {

    private final AuthorClient authorClient;

    public AuthorMvcController(AuthorClient authorClient) {
        this.authorClient = authorClient;
    }

    @GetMapping("/operations")
    public String showOperationsMenu() {
        return "authors/author-operations";
    }

    // --- CRUD OPERATIONS ---
    @GetMapping("/get-all")
    public String getAllAuthors(Model model) {
        model.addAttribute("authors", authorClient.getAllAuthors());
        model.addAttribute("pageTitle", "All Author Records");
        return "authors/author-list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("author", new AuthorDto());
        model.addAttribute("formTitle", "Create New Author");
        model.addAttribute("actionUrl", "/web/authors/create/save");
        model.addAttribute("isUpdate", false);
        return "authors/author-form";
    }

    @PostMapping("/create/save")
    public String saveAuthor(@Valid @ModelAttribute("author") AuthorDto author, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("formTitle", "Create New Author");
            model.addAttribute("actionUrl", "/web/authors/create/save");
            model.addAttribute("isUpdate", false);
            return "authors/author-form";
        }
        authorClient.createAuthor(author);
        return "redirect:/web/authors/get-all";
    }

    @GetMapping("/get-by-id")
    public String showIdRequest(Model model) {
        model.addAttribute("formTitle", "Fetch Author by ID");
        model.addAttribute("actionUrl", "/web/authors/get-by-id/result");
        return "authors/author-id-request";
    }

    @GetMapping("/get-by-id/result")
    public String getByIdResult(@RequestParam("id") String id, Model model) {
        model.addAttribute("author", authorClient.getAuthorById(id));
        return "authors/author-details";
    }

    @GetMapping("/update")
    public String showUpdateIdRequest(Model model) {
        model.addAttribute("formTitle", "Update Author (PUT)");
        model.addAttribute("actionUrl", "/web/authors/update/form");
        return "authors/author-id-request";
    }

    @GetMapping("/update/form")
    public String showUpdateForm(@RequestParam("id") String id, Model model) {
        model.addAttribute("author", authorClient.getAuthorById(id));
        model.addAttribute("formTitle", "Update Author Data");
        model.addAttribute("actionUrl", "/web/authors/update/save");
        model.addAttribute("isUpdate", true);
        return "authors/author-form";
    }

    @PostMapping("/update/save")
    public String saveUpdate(@Valid @ModelAttribute("author") AuthorDto author, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("formTitle", "Update Author Data");
            model.addAttribute("actionUrl", "/web/authors/update/save");
            model.addAttribute("isUpdate", true);
            return "authors/author-form";
        }
        authorClient.updateAuthor(author.getAuId(), author);
        return "redirect:/web/authors/get-all";
    }

    @GetMapping("/patch")
    public String showPatchIdRequest(Model model) {
        model.addAttribute("formTitle", "Patch Author");
        model.addAttribute("actionUrl", "/web/authors/patch/form");
        return "authors/author-id-request";
    }

    @GetMapping("/patch/form")
    public String showPatchForm(@RequestParam("id") String id, Model model) {
        model.addAttribute("author", authorClient.getAuthorById(id));
        model.addAttribute("formTitle", "Patch Author Data");
        model.addAttribute("actionUrl", "/web/authors/patch/save");
        model.addAttribute("isUpdate", true);
        return "authors/author-form";
    }

    @PostMapping("/patch/save")
    public String savePatch(@ModelAttribute("author") AuthorDto author) {
        authorClient.patchAuthor(author.getAuId(), author);
        return "redirect:/web/authors/get-all";
    }

    // --- FILTERS ---
    @GetMapping("/filter/contract")
    public String filterContract(Model model) {
        model.addAttribute("formTitle", "Filter by Contract Status");
        model.addAttribute("actionUrl", "/web/authors/filter/contract/result");
        model.addAttribute("paramName", "status");
        model.addAttribute("inputType", "number");
        return "authors/author-single-param-request";
    }

    @GetMapping("/filter/contract/result")
    public String filterContractResult(@RequestParam("status") Integer status, Model model) {
        model.addAttribute("authors", authorClient.filterAuthorsByContract(status));
        model.addAttribute("pageTitle", "Authors with Contract Status: " + status);
        return "authors/author-list";
    }

    @GetMapping("/filter/city")
    public String filterCity(Model model) {
        model.addAttribute("formTitle", "Filter by City");
        model.addAttribute("actionUrl", "/web/authors/filter/city/result");
        model.addAttribute("paramName", "city");
        model.addAttribute("inputType", "text");
        return "authors/author-single-param-request";
    }

    @GetMapping("/filter/city/result")
    public String filterCityResult(@RequestParam("city") String city, Model model) {
        model.addAttribute("authors", authorClient.filterAuthorsByCity(city));
        model.addAttribute("pageTitle", "Authors in City: " + city);
        return "authors/author-list";
    }

    @GetMapping("/filter/state")
    public String filterState(Model model) {
        model.addAttribute("formTitle", "Filter by State");
        model.addAttribute("actionUrl", "/web/authors/filter/state/result");
        model.addAttribute("paramName", "state");
        model.addAttribute("inputType", "text");
        return "authors/author-single-param-request";
    }

    @GetMapping("/filter/state/result")
    public String filterStateResult(@RequestParam("state") String state, Model model) {
        model.addAttribute("authors", authorClient.filterAuthorsByState(state));
        model.addAttribute("pageTitle", "Authors in State: " + state);
        return "authors/author-list";
    }

    // --- RELATIONAL (Spiderweb) ---
    @GetMapping("/relational/title-authors")
    public String relTitleAuthors(Model model) {
        model.addAttribute("formTitle", "Get Contracts by Author");
        model.addAttribute("actionUrl", "/web/authors/relational/title-authors/result");
        return "authors/author-id-request";
    }

    @GetMapping("/relational/title-authors/result")
    public String relTitleAuthorsResult(@RequestParam("id") String id, Model model) {
        model.addAttribute("targetId", id);
        model.addAttribute("titleAuthors", authorClient.getTitleAuthorsByAuthor(id));
        return "authors/author-title-authors";
    }

    @GetMapping("/relational/titles")
    public String relTitles(Model model) {
        model.addAttribute("formTitle", "Get Titles by Author");
        model.addAttribute("actionUrl", "/web/authors/relational/titles/result");
        return "authors/author-id-request";
    }

    @GetMapping("/relational/titles/result")
    public String relTitlesResult(@RequestParam("id") String id, Model model) {
        model.addAttribute("targetId", id);
        model.addAttribute("titles", authorClient.getTitlesByAuthor(id));
        return "authors/author-titles";
    }

    @GetMapping("/relational/publishers")
    public String relPublishers(Model model) {
        model.addAttribute("formTitle", "Get Publishers by Author");
        model.addAttribute("actionUrl", "/web/authors/relational/publishers/result");
        return "authors/author-id-request";
    }

    @GetMapping("/relational/publishers/result")
    public String relPublishersResult(@RequestParam("id") String id, Model model) {
        model.addAttribute("targetId", id);
        model.addAttribute("publishers", authorClient.getPublishersByAuthor(id));
        return "authors/author-publishers";
    }

    @GetMapping("/relational/stores")
    public String relStores(Model model) {
        model.addAttribute("formTitle", "Get Stores by Author");
        model.addAttribute("actionUrl", "/web/authors/relational/stores/result");
        return "authors/author-id-request";
    }

    @GetMapping("/relational/stores/result")
    public String relStoresResult(@RequestParam("id") String id, Model model) {
        model.addAttribute("targetId", id);
        model.addAttribute("stores", authorClient.getStoresByAuthor(id));
        return "authors/author-stores";
    }
}