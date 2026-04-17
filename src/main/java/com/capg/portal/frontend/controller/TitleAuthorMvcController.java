package com.capg.portal.frontend.controller;

import com.capg.portal.frontend.client.TitleAuthorClient;
import com.capg.portal.frontend.dto.AuthorDto;
import com.capg.portal.frontend.dto.TitleAuthorDto;
import com.capg.portal.frontend.dto.TitleDto;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/web/titleauthors")
public class TitleAuthorMvcController {

    private final TitleAuthorClient titleAuthorClient;

    public TitleAuthorMvcController(TitleAuthorClient titleAuthorClient) {
        this.titleAuthorClient = titleAuthorClient;
    }

    @GetMapping("/operations")
    public String showOperationsMenu() {
        return "titleauthors/titleauthor-operations";
    }

    // --- CRUD ---
    @GetMapping("/get-all")
    public String getAll(Model model) {
        model.addAttribute("titleAuthors", titleAuthorClient.getAllTitleAuthors());
        model.addAttribute("pageTitle", "All Title-Author Mappings");
        return "titleauthors/titleauthor-list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        TitleAuthorDto dto = new TitleAuthorDto();
        dto.setAuthor(new AuthorDto());
        dto.setTitle(new TitleDto());
        
        model.addAttribute("titleAuthor", dto);
        model.addAttribute("formTitle", "Map Author to Title");
        model.addAttribute("actionUrl", "/web/titleauthors/create/save");
        model.addAttribute("isUpdate", false);
        return "titleauthors/titleauthor-form";
    }

    @PostMapping("/create/save")
    public String saveCreate(@Valid @ModelAttribute("titleAuthor") TitleAuthorDto titleAuthor, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("formTitle", "Map Author to Title");
            model.addAttribute("actionUrl", "/web/titleauthors/create/save");
            model.addAttribute("isUpdate", false);
            return "titleauthors/titleauthor-form";
        }
        try {
            titleAuthorClient.createTitleAuthor(titleAuthor);
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error: Mapping already exists, or Total Royalty limit (100%) exceeded.");
            model.addAttribute("formTitle", "Map Author to Title");
            model.addAttribute("actionUrl", "/web/titleauthors/create/save");
            model.addAttribute("isUpdate", false);
            return "titleauthors/titleauthor-form";
        }
        return "redirect:/web/titleauthors/get-all";
    }

    @GetMapping("/get-by-id")
    public String showIdRequest(Model model) {
        model.addAttribute("formTitle", "Fetch Mapping by Keys");
        model.addAttribute("actionUrl", "/web/titleauthors/get-by-id/result");
        return "titleauthors/titleauthor-id-request";
    }

    @GetMapping("/get-by-id/result")
    public String getIdResult(@RequestParam("auId") String auId, @RequestParam("titleId") String titleId, Model model) {
        model.addAttribute("titleAuthor", titleAuthorClient.getTitleAuthorById(auId, titleId));
        return "titleauthors/titleauthor-details";
    }

    @GetMapping("/update")
    public String showUpdateIdRequest(Model model) {
        model.addAttribute("formTitle", "Update Mapping (PUT)");
        model.addAttribute("actionUrl", "/web/titleauthors/update/form");
        return "titleauthors/titleauthor-id-request";
    }

    @GetMapping("/update/form")
    public String showUpdateForm(@RequestParam("auId") String auId, @RequestParam("titleId") String titleId, Model model) {
        TitleAuthorDto ta = titleAuthorClient.getTitleAuthorById(auId, titleId);
        
        model.addAttribute("titleAuthor", ta);
        model.addAttribute("formTitle", "Update Contract Data");
        model.addAttribute("actionUrl", "/web/titleauthors/update/save");
        model.addAttribute("isUpdate", true);
        return "titleauthors/titleauthor-form";
    }

    @PostMapping("/update/save")
    public String saveUpdate(@Valid @ModelAttribute("titleAuthor") TitleAuthorDto titleAuthor, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("formTitle", "Update Contract Data");
            model.addAttribute("actionUrl", "/web/titleauthors/update/save");
            model.addAttribute("isUpdate", true);
            return "titleauthors/titleauthor-form";
        }
        try {
            titleAuthorClient.updateTitleAuthor(titleAuthor.getAuthor().getAuId(), titleAuthor.getTitle().getTitleId(), titleAuthor);
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error: Total Royalty limit (100%) exceeded for this title.");
            model.addAttribute("formTitle", "Update Contract Data");
            model.addAttribute("actionUrl", "/web/titleauthors/update/save");
            model.addAttribute("isUpdate", true);
            return "titleauthors/titleauthor-form";
        }
        return "redirect:/web/titleauthors/get-all";
    }

    @GetMapping("/patch")
    public String showPatchIdRequest(Model model) {
        model.addAttribute("formTitle", "Patch Mapping");
        model.addAttribute("actionUrl", "/web/titleauthors/patch/form");
        return "titleauthors/titleauthor-id-request";
    }

    @GetMapping("/patch/form")
    public String showPatchForm(@RequestParam("auId") String auId, @RequestParam("titleId") String titleId, Model model) {
        TitleAuthorDto ta = titleAuthorClient.getTitleAuthorById(auId, titleId);
        
        model.addAttribute("titleAuthor", ta);
        model.addAttribute("formTitle", "Patch Contract Data");
        model.addAttribute("actionUrl", "/web/titleauthors/patch/save");
        model.addAttribute("isUpdate", true);
        return "titleauthors/titleauthor-form";
    }

    @PostMapping("/patch/save")
    public String savePatch(@ModelAttribute("titleAuthor") TitleAuthorDto titleAuthor, Model model) {
        try {
            titleAuthorClient.patchTitleAuthor(titleAuthor.getAuthor().getAuId(), titleAuthor.getTitle().getTitleId(), titleAuthor);
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error: Total Royalty limit (100%) exceeded for this title.");
            model.addAttribute("formTitle", "Patch Contract Data");
            model.addAttribute("actionUrl", "/web/titleauthors/patch/save");
            model.addAttribute("isUpdate", true);
            return "titleauthors/titleauthor-form";
        }
        return "redirect:/web/titleauthors/get-all";
    }

    // --- FILTERS & SEARCH ---
    @GetMapping("/filter/lead")
    public String getLeadAuthors(Model model) {
        model.addAttribute("titleAuthors", titleAuthorClient.getLeadAuthors());
        model.addAttribute("pageTitle", "Lead Authors (auOrd = 1)");
        return "titleauthors/titleauthor-list";
    }

    @GetMapping("/filter/author")
    public String filterAuthor(Model model) {
        model.addAttribute("formTitle", "Find Contracts by Author ID");
        model.addAttribute("actionUrl", "/web/titleauthors/filter/author/result");
        model.addAttribute("paramLabel", "Author ID");
        model.addAttribute("paramName", "auId");
        return "titleauthors/titleauthor-single-param-request";
    }

    @GetMapping("/filter/author/result")
    public String filterAuthorResult(@RequestParam("auId") String auId, Model model) {
        model.addAttribute("titleAuthors", titleAuthorClient.filterTitleAuthorsByAuthorId(auId));
        model.addAttribute("pageTitle", "Contracts for Author: " + auId);
        return "titleauthors/titleauthor-list";
    }

    @GetMapping("/filter/title")
    public String filterTitle(Model model) {
        model.addAttribute("formTitle", "Find Authors by Title ID");
        model.addAttribute("actionUrl", "/web/titleauthors/filter/title/result");
        model.addAttribute("paramLabel", "Title ID");
        model.addAttribute("paramName", "titleId");
        return "titleauthors/titleauthor-single-param-request";
    }

    @GetMapping("/filter/title/result")
    public String filterTitleResult(@RequestParam("titleId") String titleId, Model model) {
        model.addAttribute("titleAuthors", titleAuthorClient.filterTitleAuthorsByTitleId(titleId));
        model.addAttribute("pageTitle", "Authors assigned to Title: " + titleId);
        return "titleauthors/titleauthor-list";
    }

    @GetMapping("/filter/royalty")
    public String filterRoyalty(Model model) {
        model.addAttribute("formTitle", "Find by Maximum Royalty");
        model.addAttribute("actionUrl", "/web/titleauthors/filter/royalty/result");
        model.addAttribute("paramLabel", "Max Royalty %");
        model.addAttribute("paramName", "maxRoyalty");
        return "titleauthors/titleauthor-single-param-request";
    }

    @GetMapping("/filter/royalty/result")
    public String filterRoyaltyResult(@RequestParam("maxRoyalty") Integer maxRoyalty, Model model) {
        model.addAttribute("titleAuthors", titleAuthorClient.filterTitleAuthorsByRoyalty(maxRoyalty));
        model.addAttribute("pageTitle", "Contracts with Royalty <= " + maxRoyalty + "%");
        return "titleauthors/titleauthor-list";
    }

    // --- DYNAMIC SEARCH ---
    @GetMapping("/search")
    public String searchForm() {
        return "titleauthors/titleauthor-search-request";
    }

    @GetMapping("/search/result")
    public String searchResult(
            @RequestParam(value = "auId", required = false) String auId,
            @RequestParam(value = "titleId", required = false) String titleId,
            @RequestParam(value = "maxRoyalty", required = false) Integer maxRoyalty,
            @RequestParam(value = "minRoyalty", required = false) Integer minRoyalty,
            Model model) {
        
        // Clean up empty strings from the HTML form to proper nulls
        if (auId != null && auId.trim().isEmpty()) auId = null;
        if (titleId != null && titleId.trim().isEmpty()) titleId = null;

        model.addAttribute("titleAuthors", titleAuthorClient.search(auId, titleId, maxRoyalty, minRoyalty));
        model.addAttribute("pageTitle", "Dynamic Search Results");
        return "titleauthors/titleauthor-list";
    }
}