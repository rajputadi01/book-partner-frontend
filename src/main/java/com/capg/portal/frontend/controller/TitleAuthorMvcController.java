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
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Controller
@RequestMapping("/web/titleauthors")
public class TitleAuthorMvcController {

    private final TitleAuthorClient titleAuthorClient;
    private final int PAGE_SIZE = 5;

    public TitleAuthorMvcController(TitleAuthorClient titleAuthorClient) {
        this.titleAuthorClient = titleAuthorClient;
    }

    @GetMapping("/operations")
    public String showOperationsMenu() {
        return "titleauthors/titleauthor-operations";
    }

    // --- CRUD ---
    @GetMapping("/get-all")
    public String getAll(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "sortBy", defaultValue = "auId") String sortBy,
            @RequestParam(value = "dir", defaultValue = "asc") String dir,
            Model model) {
        
        List<TitleAuthorDto> list = titleAuthorClient.getAllTitleAuthors();
        sortTitleAuthors(list, sortBy, dir);

        model.addAttribute("titleAuthors", paginateList(list, page, PAGE_SIZE, model));
        model.addAttribute("pageTitle", "All Title-Author Mappings");
        model.addAttribute("endpoint", "/web/titleauthors/get-all");
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("dir", dir);
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
        // Pre-Flight Validation: Royalty must be between 0 and 100
        if (titleAuthor.getRoyaltyPer() != null && (titleAuthor.getRoyaltyPer() < 0 || titleAuthor.getRoyaltyPer() > 100)) {
            result.rejectValue("royaltyPer", "error.titleAuthor", "Royalty percentage must be between 0 and 100.");
        }
        
        if (result.hasErrors()) {
            model.addAttribute("formTitle", "Map Author to Title");
            model.addAttribute("actionUrl", "/web/titleauthors/create/save");
            model.addAttribute("isUpdate", false);
            return "titleauthors/titleauthor-form";
        }
        
        try {
            titleAuthorClient.createTitleAuthor(titleAuthor);
            return "redirect:/web/titleauthors/get-all";
        } catch (HttpStatusCodeException e) {
            model.addAttribute("errorMessage", extractErrorMessage(e));
            model.addAttribute("formTitle", "Map Author to Title");
            model.addAttribute("actionUrl", "/web/titleauthors/create/save");
            model.addAttribute("isUpdate", false);
            return "titleauthors/titleauthor-form";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "An unexpected error occurred: " + e.getMessage());
            model.addAttribute("formTitle", "Map Author to Title");
            model.addAttribute("actionUrl", "/web/titleauthors/create/save");
            model.addAttribute("isUpdate", false);
            return "titleauthors/titleauthor-form";
        }
    }

    @GetMapping("/get-by-id")
    public String showIdRequest(Model model) {
        model.addAttribute("formTitle", "Fetch Mapping by Keys");
        model.addAttribute("actionUrl", "/web/titleauthors/get-by-id/result");
        return "titleauthors/titleauthor-id-request";
    }

    @GetMapping("/get-by-id/result")
    public String getIdResult(@RequestParam("auId") String auId, @RequestParam("titleId") String titleId, Model model, RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("titleAuthor", titleAuthorClient.getTitleAuthorById(auId, titleId));
            return "titleauthors/titleauthor-details";
        } catch (HttpStatusCodeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", extractErrorMessage(e));
            return "redirect:/web/titleauthors/get-by-id";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred: " + e.getMessage());
            return "redirect:/web/titleauthors/get-by-id";
        }
    }

    @GetMapping("/update")
    public String showUpdateIdRequest(Model model) {
        model.addAttribute("formTitle", "Update Mapping (PUT)");
        model.addAttribute("actionUrl", "/web/titleauthors/update/form");
        return "titleauthors/titleauthor-id-request";
    }

    @GetMapping("/update/form")
    public String showUpdateForm(@RequestParam("auId") String auId, @RequestParam("titleId") String titleId, Model model, RedirectAttributes redirectAttributes) {
        try {
            TitleAuthorDto ta = titleAuthorClient.getTitleAuthorById(auId, titleId);
            model.addAttribute("titleAuthor", ta);
            model.addAttribute("formTitle", "Update Contract Data");
            model.addAttribute("actionUrl", "/web/titleauthors/update/save");
            model.addAttribute("isUpdate", true);
            return "titleauthors/titleauthor-form";
        } catch (HttpStatusCodeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", extractErrorMessage(e));
            return "redirect:/web/titleauthors/update";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred: " + e.getMessage());
            return "redirect:/web/titleauthors/update";
        }
    }

    @PostMapping("/update/save")
    public String saveUpdate(@Valid @ModelAttribute("titleAuthor") TitleAuthorDto titleAuthor, BindingResult result, Model model) {
        if (titleAuthor.getRoyaltyPer() != null && (titleAuthor.getRoyaltyPer() < 0 || titleAuthor.getRoyaltyPer() > 100)) {
            result.rejectValue("royaltyPer", "error.titleAuthor", "Royalty percentage must be between 0 and 100.");
        }
        
        if (result.hasErrors()) {
            model.addAttribute("formTitle", "Update Contract Data");
            model.addAttribute("actionUrl", "/web/titleauthors/update/save");
            model.addAttribute("isUpdate", true);
            return "titleauthors/titleauthor-form";
        }
        
        try {
            titleAuthorClient.updateTitleAuthor(titleAuthor.getAuthor().getAuId(), titleAuthor.getTitle().getTitleId(), titleAuthor);
            return "redirect:/web/titleauthors/get-all";
        } catch (HttpStatusCodeException e) {
            model.addAttribute("errorMessage", extractErrorMessage(e));
            model.addAttribute("formTitle", "Update Contract Data");
            model.addAttribute("actionUrl", "/web/titleauthors/update/save");
            model.addAttribute("isUpdate", true);
            return "titleauthors/titleauthor-form";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "An unexpected error occurred: " + e.getMessage());
            model.addAttribute("formTitle", "Update Contract Data");
            model.addAttribute("actionUrl", "/web/titleauthors/update/save");
            model.addAttribute("isUpdate", true);
            return "titleauthors/titleauthor-form";
        }
    }

    @GetMapping("/patch")
    public String showPatchIdRequest(Model model) {
        model.addAttribute("formTitle", "Patch Mapping");
        model.addAttribute("actionUrl", "/web/titleauthors/patch/form");
        return "titleauthors/titleauthor-id-request";
    }

    @GetMapping("/patch/form")
    public String showPatchForm(@RequestParam("auId") String auId, @RequestParam("titleId") String titleId, Model model, RedirectAttributes redirectAttributes) {
        try {
            // Read-Only Existing Data
            model.addAttribute("currentRecord", titleAuthorClient.getTitleAuthorById(auId, titleId));
            
            // Blank Patch DTO
            TitleAuthorDto patchDto = new TitleAuthorDto();
            patchDto.setAuthor(new AuthorDto());
            patchDto.getAuthor().setAuId(auId);
            patchDto.setTitle(new TitleDto());
            patchDto.getTitle().setTitleId(titleId);
            
            model.addAttribute("titleAuthor", patchDto);
            model.addAttribute("formTitle", "Patch Contract Data");
            model.addAttribute("actionUrl", "/web/titleauthors/patch/save");
            return "titleauthors/titleauthor-patch";
        } catch (HttpStatusCodeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", extractErrorMessage(e));
            return "redirect:/web/titleauthors/patch";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred: " + e.getMessage());
            return "redirect:/web/titleauthors/patch";
        }
    }

    @PostMapping("/patch/save")
    public String savePatch(@ModelAttribute("titleAuthor") TitleAuthorDto titleAuthor, RedirectAttributes redirectAttributes) {
        if (titleAuthor.getRoyaltyPer() != null && (titleAuthor.getRoyaltyPer() < 0 || titleAuthor.getRoyaltyPer() > 100)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Validation Error: Royalty percentage must be between 0 and 100.");
            return "redirect:/web/titleauthors/patch/form?auId=" + titleAuthor.getAuthor().getAuId() + "&titleId=" + titleAuthor.getTitle().getTitleId();
        }
        
        try {
            titleAuthorClient.patchTitleAuthor(titleAuthor.getAuthor().getAuId(), titleAuthor.getTitle().getTitleId(), titleAuthor);
            return "redirect:/web/titleauthors/get-all";
        } catch (HttpStatusCodeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", extractErrorMessage(e));
            return "redirect:/web/titleauthors/patch/form?auId=" + titleAuthor.getAuthor().getAuId() + "&titleId=" + titleAuthor.getTitle().getTitleId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred: " + e.getMessage());
            return "redirect:/web/titleauthors/patch/form?auId=" + titleAuthor.getAuthor().getAuId() + "&titleId=" + titleAuthor.getTitle().getTitleId();
        }
    }

    // --- FILTERS & SEARCH ---
    @GetMapping("/filter/lead")
    public String getLeadAuthors(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "sortBy", defaultValue = "auId") String sortBy,
            @RequestParam(value = "dir", defaultValue = "asc") String dir,
            Model model, RedirectAttributes redirectAttributes) {
        try {
            List<TitleAuthorDto> list = titleAuthorClient.getLeadAuthors();
            sortTitleAuthors(list, sortBy, dir);

            model.addAttribute("titleAuthors", paginateList(list, page, PAGE_SIZE, model));
            model.addAttribute("pageTitle", "Lead Authors (auOrd = 1)");
            model.addAttribute("endpoint", "/web/titleauthors/filter/lead");
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("dir", dir);
            return "titleauthors/titleauthor-list";
        } catch (HttpStatusCodeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", extractErrorMessage(e));
            return "redirect:/web/titleauthors/operations";
        }
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
    public String filterAuthorResult(
            @RequestParam("auId") String auId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "sortBy", defaultValue = "titleId") String sortBy,
            @RequestParam(value = "dir", defaultValue = "asc") String dir,
            Model model, RedirectAttributes redirectAttributes) {
        try {
            List<TitleAuthorDto> list = titleAuthorClient.filterTitleAuthorsByAuthorId(auId);
            sortTitleAuthors(list, sortBy, dir);

            model.addAttribute("titleAuthors", paginateList(list, page, PAGE_SIZE, model));
            model.addAttribute("pageTitle", "Contracts for Author: " + auId);
            model.addAttribute("endpoint", "/web/titleauthors/filter/author/result");
            model.addAttribute("auId", auId);
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("dir", dir);
            return "titleauthors/titleauthor-list";
        } catch (HttpStatusCodeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", extractErrorMessage(e));
            return "redirect:/web/titleauthors/filter/author";
        }
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
    public String filterTitleResult(
            @RequestParam("titleId") String titleId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "sortBy", defaultValue = "auId") String sortBy,
            @RequestParam(value = "dir", defaultValue = "asc") String dir,
            Model model, RedirectAttributes redirectAttributes) {
        try {
            List<TitleAuthorDto> list = titleAuthorClient.filterTitleAuthorsByTitleId(titleId);
            sortTitleAuthors(list, sortBy, dir);

            model.addAttribute("titleAuthors", paginateList(list, page, PAGE_SIZE, model));
            model.addAttribute("pageTitle", "Authors assigned to Title: " + titleId);
            model.addAttribute("endpoint", "/web/titleauthors/filter/title/result");
            model.addAttribute("titleId", titleId);
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("dir", dir);
            return "titleauthors/titleauthor-list";
        } catch (HttpStatusCodeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", extractErrorMessage(e));
            return "redirect:/web/titleauthors/filter/title";
        }
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
    public String filterRoyaltyResult(
            @RequestParam("maxRoyalty") Integer maxRoyalty,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "sortBy", defaultValue = "auId") String sortBy,
            @RequestParam(value = "dir", defaultValue = "asc") String dir,
            Model model, RedirectAttributes redirectAttributes) {
        try {
            List<TitleAuthorDto> list = titleAuthorClient.filterTitleAuthorsByRoyalty(maxRoyalty);
            sortTitleAuthors(list, sortBy, dir);

            model.addAttribute("titleAuthors", paginateList(list, page, PAGE_SIZE, model));
            model.addAttribute("pageTitle", "Contracts with Royalty <= " + maxRoyalty + "%");
            model.addAttribute("endpoint", "/web/titleauthors/filter/royalty/result");
            model.addAttribute("maxRoyalty", maxRoyalty);
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("dir", dir);
            return "titleauthors/titleauthor-list";
        } catch (HttpStatusCodeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", extractErrorMessage(e));
            return "redirect:/web/titleauthors/filter/royalty";
        }
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
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "sortBy", defaultValue = "auId") String sortBy,
            @RequestParam(value = "dir", defaultValue = "asc") String dir,
            Model model, RedirectAttributes redirectAttributes) {
        
        // Clean up empty strings from the HTML form to proper nulls
        if (auId != null && auId.trim().isEmpty()) auId = null;
        if (titleId != null && titleId.trim().isEmpty()) titleId = null;

        try {
            List<TitleAuthorDto> list = titleAuthorClient.search(auId, titleId, maxRoyalty, minRoyalty);
            sortTitleAuthors(list, sortBy, dir);

            model.addAttribute("titleAuthors", paginateList(list, page, PAGE_SIZE, model));
            model.addAttribute("pageTitle", "Dynamic Search Results");
            model.addAttribute("endpoint", "/web/titleauthors/search/result");
            model.addAttribute("auId", auId);
            model.addAttribute("titleId", titleId);
            model.addAttribute("maxRoyalty", maxRoyalty);
            model.addAttribute("minRoyalty", minRoyalty);
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("dir", dir);
            return "titleauthors/titleauthor-list";
        } catch (HttpStatusCodeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", extractErrorMessage(e));
            return "redirect:/web/titleauthors/search";
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

    private void sortTitleAuthors(List<TitleAuthorDto> list, String sortBy, String dir) {
        if ("auId".equals(sortBy)) list.sort(Comparator.comparing(ta -> ta.getAuthor() != null ? ta.getAuthor().getAuId() : "", Comparator.nullsLast(String::compareToIgnoreCase)));
        else if ("titleId".equals(sortBy)) list.sort(Comparator.comparing(ta -> ta.getTitle() != null ? ta.getTitle().getTitleId() : "", Comparator.nullsLast(String::compareToIgnoreCase)));
        else if ("auOrd".equals(sortBy)) list.sort(Comparator.comparing(TitleAuthorDto::getAuOrd, Comparator.nullsLast(Comparator.naturalOrder())));
        else if ("royaltyPer".equals(sortBy)) list.sort(Comparator.comparing(TitleAuthorDto::getRoyaltyPer, Comparator.nullsLast(Comparator.naturalOrder())));
        else list.sort(Comparator.comparing(ta -> ta.getAuthor() != null ? ta.getAuthor().getAuId() : ""));
        
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