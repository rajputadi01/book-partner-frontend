package com.capg.portal.frontend.controller;

import com.capg.portal.frontend.client.TitleClient;
import com.capg.portal.frontend.dto.PublisherDto;
import com.capg.portal.frontend.dto.TitleDto;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/web/titles")
public class TitleMvcController {

    private final TitleClient titleClient;

    public TitleMvcController(TitleClient titleClient) {
        this.titleClient = titleClient;
    }

    @GetMapping("/operations")
    public String showOperationsMenu() {
        return "titles/title-operations";
    }

    // --- CRUD ---
    @GetMapping("/get-all")
    public String getAll(Model model) {
        model.addAttribute("titles", titleClient.getAllTitles());
        model.addAttribute("pageTitle", "All Book Titles");
        return "titles/title-list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        TitleDto dto = new TitleDto();
        dto.setPublisher(new PublisherDto()); // Prevent null reference on form load
        model.addAttribute("title", dto);
        model.addAttribute("formTitle", "Register New Title");
        model.addAttribute("actionUrl", "/web/titles/create/save");
        model.addAttribute("isUpdate", false);
        return "titles/title-form";
    }

    @PostMapping("/create/save")
    public String saveCreate(@Valid @ModelAttribute("title") TitleDto title, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("formTitle", "Register New Title");
            model.addAttribute("actionUrl", "/web/titles/create/save");
            model.addAttribute("isUpdate", false);
            return "titles/title-form";
        }
        cleanEmptyPublisherId(title);
        titleClient.createTitle(title);
        return "redirect:/web/titles/get-all";
    }

    @GetMapping("/get-by-id")
    public String showIdRequest(Model model) {
        model.addAttribute("formTitle", "Fetch Title by ID");
        model.addAttribute("actionUrl", "/web/titles/get-by-id/result");
        return "titles/title-id-request";
    }

    @GetMapping("/get-by-id/result")
    public String getIdResult(@RequestParam("id") String id, Model model) {
        model.addAttribute("title", titleClient.getTitleById(id));
        return "titles/title-details";
    }

    @GetMapping("/update")
    public String showUpdateIdRequest(Model model) {
        model.addAttribute("formTitle", "Update Title (PUT)");
        model.addAttribute("actionUrl", "/web/titles/update/form");
        return "titles/title-id-request";
    }

    @GetMapping("/update/form")
    public String showUpdateForm(@RequestParam("id") String id, Model model) {
        TitleDto title = titleClient.getTitleById(id);
        if (title.getPublisher() == null) title.setPublisher(new PublisherDto());

        model.addAttribute("title", title);
        model.addAttribute("formTitle", "Update Title Data");
        model.addAttribute("actionUrl", "/web/titles/update/save");
        model.addAttribute("isUpdate", true);
        return "titles/title-form";
    }

    @PostMapping("/update/save")
    public String saveUpdate(@Valid @ModelAttribute("title") TitleDto title, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("formTitle", "Update Title Data");
            model.addAttribute("actionUrl", "/web/titles/update/save");
            model.addAttribute("isUpdate", true);
            return "titles/title-form";
        }
        cleanEmptyPublisherId(title);
        titleClient.updateTitle(title.getTitleId(), title);
        return "redirect:/web/titles/get-all";
    }

    @GetMapping("/patch")
    public String showPatchIdRequest(Model model) {
        model.addAttribute("formTitle", "Patch Title");
        model.addAttribute("actionUrl", "/web/titles/patch/form");
        return "titles/title-id-request";
    }

    @GetMapping("/patch/form")
    public String showPatchForm(@RequestParam("id") String id, Model model) {
        TitleDto title = titleClient.getTitleById(id);
        if (title.getPublisher() == null) title.setPublisher(new PublisherDto());

        model.addAttribute("title", title);
        model.addAttribute("formTitle", "Patch Title Data");
        model.addAttribute("actionUrl", "/web/titles/patch/save");
        model.addAttribute("isUpdate", true);
        return "titles/title-form";
    }

    @PostMapping("/patch/save")
    public String savePatch(@ModelAttribute("title") TitleDto title) {
        cleanEmptyPublisherId(title);
        titleClient.patchTitle(title.getTitleId(), title);
        return "redirect:/web/titles/get-all";
    }

    // --- FILTERS ---
    @GetMapping("/filter/price")
    public String filterPrice(Model model) {
        model.addAttribute("formTitle", "Filter by Max Price");
        model.addAttribute("actionUrl", "/web/titles/filter/price/result");
        model.addAttribute("paramName", "maxPrice");
        model.addAttribute("inputType", "number");
        model.addAttribute("step", "0.01");
        return "titles/title-single-param-request";
    }

    @GetMapping("/filter/price/result")
    public String filterPriceResult(@RequestParam("maxPrice") Double maxPrice, Model model) {
        model.addAttribute("titles", titleClient.filterTitlesByPrice(maxPrice));
        model.addAttribute("pageTitle", "Titles Priced Under $" + maxPrice);
        return "titles/title-list";
    }

    @GetMapping("/filter/type")
    public String filterType(Model model) {
        model.addAttribute("formTitle", "Filter by Genre/Type");
        model.addAttribute("actionUrl", "/web/titles/filter/type/result");
        model.addAttribute("paramName", "type");
        model.addAttribute("inputType", "text");
        return "titles/title-single-param-request";
    }

    @GetMapping("/filter/type/result")
    public String filterTypeResult(@RequestParam("type") String type, Model model) {
        model.addAttribute("titles", titleClient.filterTitlesByType(type));
        model.addAttribute("pageTitle", "Titles in Genre: " + type);
        return "titles/title-list";
    }

    @GetMapping("/filter/publisher")
    public String filterPublisher(Model model) {
        model.addAttribute("formTitle", "Filter by Publisher ID");
        model.addAttribute("actionUrl", "/web/titles/filter/publisher/result");
        model.addAttribute("paramName", "pubId");
        model.addAttribute("inputType", "text");
        return "titles/title-single-param-request";
    }

    @GetMapping("/filter/publisher/result")
    public String filterPublisherResult(@RequestParam("pubId") String pubId, Model model) {
        model.addAttribute("titles", titleClient.filterTitlesByPublisher(pubId));
        model.addAttribute("pageTitle", "Titles Published by: " + pubId);
        return "titles/title-list";
    }

    @GetMapping("/filter/date")
    public String filterDate(Model model) {
        model.addAttribute("formTitle", "Published Before Date");
        model.addAttribute("actionUrl", "/web/titles/filter/date/result");
        model.addAttribute("paramName", "beforeDate");
        model.addAttribute("inputType", "datetime-local");
        return "titles/title-single-param-request";
    }

    @GetMapping("/filter/date/result")
    public String filterDateResult(@RequestParam("beforeDate") LocalDateTime beforeDate, Model model) {
        model.addAttribute("titles", titleClient.filterTitlesByDateBefore(beforeDate));
        model.addAttribute("pageTitle", "Titles Published Before: " + beforeDate.toLocalDate());
        return "titles/title-list";
    }

    // --- RELATIONAL (Spiderweb) ---
    @GetMapping("/relational/publisher")
    public String relPublisher(Model model) {
        model.addAttribute("formTitle", "Get Publisher by Title");
        model.addAttribute("actionUrl", "/web/titles/relational/publisher/result");
        return "titles/title-id-request";
    }

    @GetMapping("/relational/publisher/result")
    public String relPublisherResult(@RequestParam("id") String id, Model model) {
        model.addAttribute("targetId", id);
        try {
            // The backend throws a 404 Exception if it's self-published. We catch it and pass null!
            model.addAttribute("publisher", titleClient.getPublisherByTitle(id));
        } catch (Exception e) {
            model.addAttribute("publisher", null);
        }
        return "titles/title-publisher";
    }

    @GetMapping("/relational/sales")
    public String relSales(Model model) {
        model.addAttribute("formTitle", "Get Sales by Title");
        model.addAttribute("actionUrl", "/web/titles/relational/sales/result");
        return "titles/title-id-request";
    }

    @GetMapping("/relational/sales/result")
    public String relSalesResult(@RequestParam("id") String id, Model model) {
        model.addAttribute("targetId", id);
        model.addAttribute("sales", titleClient.getSalesByTitle(id));
        return "titles/title-sales";
    }

    @GetMapping("/relational/royalties")
    public String relRoyalties(Model model) {
        model.addAttribute("formTitle", "Get Royalty Brackets by Title");
        model.addAttribute("actionUrl", "/web/titles/relational/royalties/result");
        return "titles/title-id-request";
    }

    @GetMapping("/relational/royalties/result")
    public String relRoyaltiesResult(@RequestParam("id") String id, Model model) {
        model.addAttribute("targetId", id);
        model.addAttribute("royalties", titleClient.getRoyaltiesByTitle(id));
        return "titles/title-royalties";
    }

    @GetMapping("/relational/title-authors")
    public String relTitleAuthors(Model model) {
        model.addAttribute("formTitle", "Get Contracts by Title");
        model.addAttribute("actionUrl", "/web/titles/relational/title-authors/result");
        return "titles/title-id-request";
    }

    @GetMapping("/relational/title-authors/result")
    public String relTitleAuthorsResult(@RequestParam("id") String id, Model model) {
        model.addAttribute("targetId", id);
        model.addAttribute("titleAuthors", titleClient.getTitleAuthorsByTitle(id));
        return "titles/title-titleauthors";
    }

    @GetMapping("/relational/authors")
    public String relAuthors(Model model) {
        model.addAttribute("formTitle", "Get Authors by Title");
        model.addAttribute("actionUrl", "/web/titles/relational/authors/result");
        return "titles/title-id-request";
    }

    @GetMapping("/relational/authors/result")
    public String relAuthorsResult(@RequestParam("id") String id, Model model) {
        model.addAttribute("targetId", id);
        model.addAttribute("authors", titleClient.getAuthorsByTitle(id));
        return "titles/title-authors";
    }

    @GetMapping("/relational/stores")
    public String relStores(Model model) {
        model.addAttribute("formTitle", "Get Stores Selling Title");
        model.addAttribute("actionUrl", "/web/titles/relational/stores/result");
        return "titles/title-id-request";
    }

    @GetMapping("/relational/stores/result")
    public String relStoresResult(@RequestParam("id") String id, Model model) {
        model.addAttribute("targetId", id);
        model.addAttribute("stores", titleClient.getStoresByTitle(id));
        return "titles/title-stores";
    }

    // UTILITY: Converts an empty string in the Publisher ID field into a true null for Self-Published books
    private void cleanEmptyPublisherId(TitleDto title) {
        if (title.getPublisher() != null && (title.getPublisher().getPubId() == null || title.getPublisher().getPubId().trim().isEmpty())) {
            title.setPublisher(null);
        }
    }
}