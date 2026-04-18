package com.capg.portal.frontend.controller;

import com.capg.portal.frontend.client.TitleClient;
import com.capg.portal.frontend.dto.*;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Controller
@RequestMapping("/web/titles")
public class TitleMvcController {

    private final TitleClient titleClient;
    private final int PAGE_SIZE = 5;

    public TitleMvcController(TitleClient titleClient) {
        this.titleClient = titleClient;
    }

    @GetMapping("/operations")
    public String showOperationsMenu() {
        return "titles/title-operations";
    }

    // --- CRUD ---
    @GetMapping("/get-all")
    public String getAll(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "sortBy", defaultValue = "titleId") String sortBy,
            @RequestParam(value = "dir", defaultValue = "asc") String dir,
            Model model) {
        
        List<TitleDto> list = titleClient.getAllTitles();
        sortTitles(list, sortBy, dir);

        model.addAttribute("titles", paginateList(list, page, PAGE_SIZE, model));
        model.addAttribute("pageTitle", "All Book Titles");
        model.addAttribute("endpoint", "/web/titles/get-all");
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("dir", dir);
        return "titles/title-list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        TitleDto dto = new TitleDto();
        dto.setPublisher(new PublisherDto()); 
        
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
        
        try {
            titleClient.createTitle(title);
            return "redirect:/web/titles/get-all";
        } catch (HttpStatusCodeException e) {
            model.addAttribute("errorMessage", extractErrorMessage(e));
            model.addAttribute("formTitle", "Register New Title");
            model.addAttribute("actionUrl", "/web/titles/create/save");
            model.addAttribute("isUpdate", false);
            return "titles/title-form";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "An unexpected error occurred: " + e.getMessage());
            model.addAttribute("formTitle", "Register New Title");
            model.addAttribute("actionUrl", "/web/titles/create/save");
            model.addAttribute("isUpdate", false);
            return "titles/title-form";
        }
    }

    @GetMapping("/get-by-id")
    public String showIdRequest(Model model) {
        model.addAttribute("formTitle", "Fetch Title by ID");
        model.addAttribute("actionUrl", "/web/titles/get-by-id/result");
        return "titles/title-id-request";
    }

    @GetMapping("/get-by-id/result")
    public String getIdResult(@RequestParam("id") String id, Model model, RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("title", titleClient.getTitleById(id));
            return "titles/title-details";
        } catch (HttpStatusCodeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", extractErrorMessage(e));
            return "redirect:/web/titles/get-by-id";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred: " + e.getMessage());
            return "redirect:/web/titles/get-by-id";
        }
    }

    @GetMapping("/update")
    public String showUpdateIdRequest(Model model) {
        model.addAttribute("formTitle", "Update Title (PUT)");
        model.addAttribute("actionUrl", "/web/titles/update/form");
        return "titles/title-id-request";
    }

    @GetMapping("/update/form")
    public String showUpdateForm(@RequestParam("id") String id, Model model, RedirectAttributes redirectAttributes) {
        try {
            TitleDto title = titleClient.getTitleById(id);
            if (title.getPublisher() == null) title.setPublisher(new PublisherDto());

            model.addAttribute("title", title);
            model.addAttribute("formTitle", "Update Title Data");
            model.addAttribute("actionUrl", "/web/titles/update/save");
            model.addAttribute("isUpdate", true);
            return "titles/title-form";
        } catch (HttpStatusCodeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", extractErrorMessage(e));
            return "redirect:/web/titles/update";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred: " + e.getMessage());
            return "redirect:/web/titles/update";
        }
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

        try {
            titleClient.updateTitle(title.getTitleId(), title);
            return "redirect:/web/titles/get-all";
        } catch (HttpStatusCodeException e) {
            model.addAttribute("errorMessage", extractErrorMessage(e));
            model.addAttribute("formTitle", "Update Title Data");
            model.addAttribute("actionUrl", "/web/titles/update/save");
            model.addAttribute("isUpdate", true);
            return "titles/title-form";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "An unexpected error occurred: " + e.getMessage());
            model.addAttribute("formTitle", "Update Title Data");
            model.addAttribute("actionUrl", "/web/titles/update/save");
            model.addAttribute("isUpdate", true);
            return "titles/title-form";
        }
    }

    @GetMapping("/patch")
    public String showPatchIdRequest(Model model) {
        model.addAttribute("formTitle", "Patch Title");
        model.addAttribute("actionUrl", "/web/titles/patch/form");
        return "titles/title-id-request";
    }

    @GetMapping("/patch/form")
    public String showPatchForm(@RequestParam("id") String id, Model model, RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("currentRecord", titleClient.getTitleById(id));
            
            TitleDto patchDto = new TitleDto();
            patchDto.setTitleId(id);
            patchDto.setPublisher(new PublisherDto());

            model.addAttribute("title", patchDto);
            model.addAttribute("formTitle", "Patch Title Data");
            model.addAttribute("actionUrl", "/web/titles/patch/save");
            return "titles/title-patch";
        } catch (HttpStatusCodeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", extractErrorMessage(e));
            return "redirect:/web/titles/patch";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred: " + e.getMessage());
            return "redirect:/web/titles/patch";
        }
    }

    @PostMapping("/patch/save")
    public String savePatch(@ModelAttribute("title") TitleDto title, RedirectAttributes redirectAttributes) {
        // Sanitization
        if (title.getTitleName() != null && title.getTitleName().trim().isEmpty()) title.setTitleName(null);
        if (title.getType() != null && title.getType().trim().isEmpty()) title.setType(null);
        if (title.getNotes() != null && title.getNotes().trim().isEmpty()) title.setNotes(null);
        cleanEmptyPublisherId(title);

        try {
            titleClient.patchTitle(title.getTitleId(), title);
            return "redirect:/web/titles/get-all";
        } catch (HttpStatusCodeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", extractErrorMessage(e));
            return "redirect:/web/titles/patch/form?id=" + title.getTitleId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred: " + e.getMessage());
            return "redirect:/web/titles/patch/form?id=" + title.getTitleId();
        }
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
    public String filterPriceResult(
            @RequestParam("maxPrice") Double maxPrice,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "sortBy", defaultValue = "titleId") String sortBy,
            @RequestParam(value = "dir", defaultValue = "asc") String dir,
            Model model, RedirectAttributes redirectAttributes) {
        try {
            List<TitleDto> list = titleClient.filterTitlesByPrice(maxPrice);
            sortTitles(list, sortBy, dir);

            model.addAttribute("titles", paginateList(list, page, PAGE_SIZE, model));
            model.addAttribute("pageTitle", "Titles Priced Under $" + maxPrice);
            model.addAttribute("endpoint", "/web/titles/filter/price/result");
            model.addAttribute("maxPrice", maxPrice);
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("dir", dir);
            return "titles/title-list";
        } catch (HttpStatusCodeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", extractErrorMessage(e));
            return "redirect:/web/titles/filter/price";
        }
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
    public String filterTypeResult(
            @RequestParam("type") String type,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "sortBy", defaultValue = "titleId") String sortBy,
            @RequestParam(value = "dir", defaultValue = "asc") String dir,
            Model model, RedirectAttributes redirectAttributes) {
        try {
            List<TitleDto> list = titleClient.filterTitlesByType(type);
            sortTitles(list, sortBy, dir);

            model.addAttribute("titles", paginateList(list, page, PAGE_SIZE, model));
            model.addAttribute("pageTitle", "Titles in Genre: " + type);
            model.addAttribute("endpoint", "/web/titles/filter/type/result");
            model.addAttribute("type", type);
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("dir", dir);
            return "titles/title-list";
        } catch (HttpStatusCodeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", extractErrorMessage(e));
            return "redirect:/web/titles/filter/type";
        }
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
    public String filterPublisherResult(
            @RequestParam("pubId") String pubId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "sortBy", defaultValue = "titleId") String sortBy,
            @RequestParam(value = "dir", defaultValue = "asc") String dir,
            Model model, RedirectAttributes redirectAttributes) {
        try {
            List<TitleDto> list = titleClient.filterTitlesByPublisher(pubId);
            sortTitles(list, sortBy, dir);

            model.addAttribute("titles", paginateList(list, page, PAGE_SIZE, model));
            model.addAttribute("pageTitle", "Titles Published by: " + pubId);
            model.addAttribute("endpoint", "/web/titles/filter/publisher/result");
            model.addAttribute("pubId", pubId);
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("dir", dir);
            return "titles/title-list";
        } catch (HttpStatusCodeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", extractErrorMessage(e));
            return "redirect:/web/titles/filter/publisher";
        }
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
    public String filterDateResult(
            @RequestParam(value = "beforeDate", required = false) String beforeDateStr,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "sortBy", defaultValue = "titleId") String sortBy,
            @RequestParam(value = "dir", defaultValue = "asc") String dir,
            Model model, RedirectAttributes redirectAttributes) {
        
        if (beforeDateStr == null || beforeDateStr.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "A valid date must be selected.");
            return "redirect:/web/titles/filter/date";
        }
        
        try {
            LocalDateTime beforeDate = LocalDateTime.parse(beforeDateStr);
            List<TitleDto> list = titleClient.filterTitlesByDateBefore(beforeDate);
            sortTitles(list, sortBy, dir);

            model.addAttribute("titles", paginateList(list, page, PAGE_SIZE, model));
            model.addAttribute("pageTitle", "Titles Published Before: " + beforeDate.toLocalDate());
            model.addAttribute("endpoint", "/web/titles/filter/date/result");
            model.addAttribute("beforeDate", beforeDateStr);
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("dir", dir);
            return "titles/title-list";
        } catch (HttpStatusCodeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", extractErrorMessage(e));
            return "redirect:/web/titles/filter/date";
        }
    }

    // --- RELATIONAL (Spiderweb) ---
    @GetMapping("/relational/publisher")
    public String relPublisher(Model model) {
        model.addAttribute("formTitle", "Get Publisher by Title");
        model.addAttribute("actionUrl", "/web/titles/relational/publisher/result");
        return "titles/title-id-request";
    }

    @GetMapping("/relational/publisher/result")
    public String relPublisherResult(@RequestParam("id") String id, Model model, RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("targetId", id);
            model.addAttribute("publisher", titleClient.getPublisherByTitle(id));
            return "titles/title-publisher";
        } catch (HttpStatusCodeException e) {
            // If backend throws 404 No Publisher Found (Self-Published), catch it gracefully
            model.addAttribute("targetId", id);
            model.addAttribute("publisher", null);
            return "titles/title-publisher";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred: " + e.getMessage());
            return "redirect:/web/titles/relational/publisher";
        }
    }

    @GetMapping("/relational/sales")
    public String relSales(Model model) {
        model.addAttribute("formTitle", "Get Sales by Title");
        model.addAttribute("actionUrl", "/web/titles/relational/sales/result");
        return "titles/title-id-request";
    }

    @GetMapping("/relational/sales/result")
    public String relSalesResult(
            @RequestParam("id") String id,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "sortBy", defaultValue = "ordNum") String sortBy,
            @RequestParam(value = "dir", defaultValue = "asc") String dir,
            Model model, RedirectAttributes redirectAttributes) {
        try {
            titleClient.getTitleById(id); // Validate title
            List<SalesDto> list = titleClient.getSalesByTitle(id);
            sortSales(list, sortBy, dir);

            model.addAttribute("targetId", id);
            model.addAttribute("sales", paginateList(list, page, PAGE_SIZE, model));
            model.addAttribute("endpoint", "/web/titles/relational/sales/result");
            model.addAttribute("id", id);
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("dir", dir);
            return "titles/title-sales";
        } catch (HttpStatusCodeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", extractErrorMessage(e));
            return "redirect:/web/titles/relational/sales";
        }
    }

    @GetMapping("/relational/royalties")
    public String relRoyalties(Model model) {
        model.addAttribute("formTitle", "Get Royalty Brackets by Title");
        model.addAttribute("actionUrl", "/web/titles/relational/royalties/result");
        return "titles/title-id-request";
    }

    @GetMapping("/relational/royalties/result")
    public String relRoyaltiesResult(
            @RequestParam("id") String id,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "sortBy", defaultValue = "lorange") String sortBy,
            @RequestParam(value = "dir", defaultValue = "asc") String dir,
            Model model, RedirectAttributes redirectAttributes) {
        try {
            titleClient.getTitleById(id); // Validate title
            List<RoyaltyScheduleDto> list = titleClient.getRoyaltiesByTitle(id);
            sortRoyalties(list, sortBy, dir);

            model.addAttribute("targetId", id);
            model.addAttribute("royalties", paginateList(list, page, PAGE_SIZE, model));
            model.addAttribute("endpoint", "/web/titles/relational/royalties/result");
            model.addAttribute("id", id);
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("dir", dir);
            return "titles/title-royalties";
        } catch (HttpStatusCodeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", extractErrorMessage(e));
            return "redirect:/web/titles/relational/royalties";
        }
    }

    @GetMapping("/relational/title-authors")
    public String relTitleAuthors(Model model) {
        model.addAttribute("formTitle", "Get Contracts by Title");
        model.addAttribute("actionUrl", "/web/titles/relational/title-authors/result");
        return "titles/title-id-request";
    }

    @GetMapping("/relational/title-authors/result")
    public String relTitleAuthorsResult(
            @RequestParam("id") String id,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "sortBy", defaultValue = "auOrd") String sortBy,
            @RequestParam(value = "dir", defaultValue = "asc") String dir,
            Model model, RedirectAttributes redirectAttributes) {
        try {
            titleClient.getTitleById(id); // Validate title
            List<TitleAuthorDto> list = titleClient.getTitleAuthorsByTitle(id);
            sortTitleAuthors(list, sortBy, dir);

            model.addAttribute("targetId", id);
            model.addAttribute("titleAuthors", paginateList(list, page, PAGE_SIZE, model));
            model.addAttribute("endpoint", "/web/titles/relational/title-authors/result");
            model.addAttribute("id", id);
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("dir", dir);
            return "titles/title-titleauthors";
        } catch (HttpStatusCodeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", extractErrorMessage(e));
            return "redirect:/web/titles/relational/title-authors";
        }
    }

    @GetMapping("/relational/authors")
    public String relAuthors(Model model) {
        model.addAttribute("formTitle", "Get Authors by Title");
        model.addAttribute("actionUrl", "/web/titles/relational/authors/result");
        return "titles/title-id-request";
    }

    @GetMapping("/relational/authors/result")
    public String relAuthorsResult(
            @RequestParam("id") String id,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "sortBy", defaultValue = "auId") String sortBy,
            @RequestParam(value = "dir", defaultValue = "asc") String dir,
            Model model, RedirectAttributes redirectAttributes) {
        try {
            titleClient.getTitleById(id); // Validate title
            List<AuthorDto> list = titleClient.getAuthorsByTitle(id);
            sortAuthors(list, sortBy, dir);

            model.addAttribute("targetId", id);
            model.addAttribute("authors", paginateList(list, page, PAGE_SIZE, model));
            model.addAttribute("endpoint", "/web/titles/relational/authors/result");
            model.addAttribute("id", id);
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("dir", dir);
            return "titles/title-authors";
        } catch (HttpStatusCodeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", extractErrorMessage(e));
            return "redirect:/web/titles/relational/authors";
        }
    }

    @GetMapping("/relational/stores")
    public String relStores(Model model) {
        model.addAttribute("formTitle", "Get Stores Selling Title");
        model.addAttribute("actionUrl", "/web/titles/relational/stores/result");
        return "titles/title-id-request";
    }

    @GetMapping("/relational/stores/result")
    public String relStoresResult(
            @RequestParam("id") String id,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "sortBy", defaultValue = "storId") String sortBy,
            @RequestParam(value = "dir", defaultValue = "asc") String dir,
            Model model, RedirectAttributes redirectAttributes) {
        try {
            titleClient.getTitleById(id); // Validate title
            List<StoreDto> list = titleClient.getStoresByTitle(id);
            sortStores(list, sortBy, dir);

            model.addAttribute("targetId", id);
            model.addAttribute("stores", paginateList(list, page, PAGE_SIZE, model));
            model.addAttribute("endpoint", "/web/titles/relational/stores/result");
            model.addAttribute("id", id);
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("dir", dir);
            return "titles/title-stores";
        } catch (HttpStatusCodeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", extractErrorMessage(e));
            return "redirect:/web/titles/relational/stores";
        }
    }

    // ==========================================
    // UTILITY ENGINES
    // ==========================================

    private void cleanEmptyPublisherId(TitleDto title) {
        if (title.getPublisher() != null && (title.getPublisher().getPubId() == null || title.getPublisher().getPubId().trim().isEmpty())) {
            title.setPublisher(null);
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

    private void sortTitles(List<TitleDto> list, String sortBy, String dir) {
        if ("titleName".equals(sortBy)) list.sort(Comparator.comparing(TitleDto::getTitleName, Comparator.nullsLast(String::compareToIgnoreCase)));
        else if ("type".equals(sortBy)) list.sort(Comparator.comparing(TitleDto::getType, Comparator.nullsLast(String::compareToIgnoreCase)));
        else if ("pubId".equals(sortBy)) list.sort(Comparator.comparing(t -> t.getPublisher() != null ? t.getPublisher().getPubId() : "ZZZZ", Comparator.nullsLast(String::compareToIgnoreCase)));
        else if ("price".equals(sortBy)) list.sort(Comparator.comparing(TitleDto::getPrice, Comparator.nullsLast(Comparator.naturalOrder())));
        else if ("pubdate".equals(sortBy)) list.sort(Comparator.comparing(TitleDto::getPubdate, Comparator.nullsLast(Comparator.naturalOrder())));
        else list.sort(Comparator.comparing(TitleDto::getTitleId, Comparator.nullsLast(String::compareToIgnoreCase)));
        
        if ("desc".equals(dir)) Collections.reverse(list);
    }

    private void sortSales(List<SalesDto> list, String sortBy, String dir) {
        if ("storId".equals(sortBy)) list.sort(Comparator.comparing(s -> s.getStore() != null ? s.getStore().getStorId() : "", Comparator.nullsLast(String::compareToIgnoreCase)));
        else if ("ordDate".equals(sortBy)) list.sort(Comparator.comparing(SalesDto::getOrdDate, Comparator.nullsLast(Comparator.naturalOrder())));
        else if ("qty".equals(sortBy)) list.sort(Comparator.comparing(SalesDto::getQty, Comparator.nullsLast(Comparator.naturalOrder())));
        else if ("payterms".equals(sortBy)) list.sort(Comparator.comparing(SalesDto::getPayterms, Comparator.nullsLast(String::compareToIgnoreCase)));
        else list.sort(Comparator.comparing(SalesDto::getOrdNum, Comparator.nullsLast(String::compareToIgnoreCase)));
        
        if ("desc".equals(dir)) Collections.reverse(list);
    }

    private void sortRoyalties(List<RoyaltyScheduleDto> list, String sortBy, String dir) {
        if ("lorange".equals(sortBy)) list.sort(Comparator.comparing(RoyaltyScheduleDto::getLorange, Comparator.nullsLast(Comparator.naturalOrder())));
        else if ("hirange".equals(sortBy)) list.sort(Comparator.comparing(RoyaltyScheduleDto::getHirange, Comparator.nullsLast(Comparator.naturalOrder())));
        else if ("royalty".equals(sortBy)) list.sort(Comparator.comparing(RoyaltyScheduleDto::getRoyalty, Comparator.nullsLast(Comparator.naturalOrder())));
        else list.sort(Comparator.comparing(RoyaltyScheduleDto::getRoyschedId, Comparator.nullsLast(Comparator.naturalOrder())));
        
        if ("desc".equals(dir)) Collections.reverse(list);
    }

    private void sortTitleAuthors(List<TitleAuthorDto> list, String sortBy, String dir) {
        if ("auId".equals(sortBy)) list.sort(Comparator.comparing(ta -> ta.getAuthor() != null ? ta.getAuthor().getAuId() : "", Comparator.nullsLast(String::compareToIgnoreCase)));
        else if ("auOrd".equals(sortBy)) list.sort(Comparator.comparing(TitleAuthorDto::getAuOrd, Comparator.nullsLast(Comparator.naturalOrder())));
        else if ("royaltyPer".equals(sortBy)) list.sort(Comparator.comparing(TitleAuthorDto::getRoyaltyPer, Comparator.nullsLast(Comparator.naturalOrder())));
        else list.sort(Comparator.comparing(ta -> ta.getAuthor() != null ? ta.getAuthor().getAuId() : ""));
        
        if ("desc".equals(dir)) Collections.reverse(list);
    }

    private void sortAuthors(List<AuthorDto> list, String sortBy, String dir) {
        if ("auFname".equals(sortBy)) list.sort(Comparator.comparing(AuthorDto::getAuFname, Comparator.nullsLast(String::compareToIgnoreCase)));
        else if ("auLname".equals(sortBy)) list.sort(Comparator.comparing(AuthorDto::getAuLname, Comparator.nullsLast(String::compareToIgnoreCase)));
        else if ("location".equals(sortBy)) list.sort(Comparator.comparing(AuthorDto::getCity, Comparator.nullsLast(String::compareToIgnoreCase)));
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