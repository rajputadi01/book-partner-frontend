package com.capg.portal.frontend.controller;

import com.capg.portal.frontend.client.StoreClient;
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
@RequestMapping("/web/stores")
public class StoreMvcController {

    private final StoreClient storeClient;
    private final int PAGE_SIZE = 5;

    public StoreMvcController(StoreClient storeClient) {
        this.storeClient = storeClient;
    }

    @GetMapping("/operations")
    public String showOperationsMenu() {
        return "stores/store-operations";
    }

    // --- CRUD ---
    @GetMapping("/get-all")
    public String getAllStores(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "sortBy", defaultValue = "storId") String sortBy,
            @RequestParam(value = "dir", defaultValue = "asc") String dir,
            Model model) {
        List<StoreDto> list = storeClient.getAllStores();
        sortStores(list, sortBy, dir);
        
        model.addAttribute("stores", paginateList(list, page, PAGE_SIZE, model));
        model.addAttribute("pageTitle", "All Store Records");
        model.addAttribute("endpoint", "/web/stores/get-all");
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("dir", dir);
        return "stores/store-list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("store", new StoreDto());
        model.addAttribute("formTitle", "Create New Store");
        model.addAttribute("actionUrl", "/web/stores/create/save");
        model.addAttribute("isUpdate", false);
        return "stores/store-form";
    }

    @PostMapping("/create/save")
    public String saveStore(@Valid @ModelAttribute("store") StoreDto store, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("formTitle", "Create New Store");
            model.addAttribute("actionUrl", "/web/stores/create/save");
            model.addAttribute("isUpdate", false);
            return "stores/store-form";
        }
        try {
            storeClient.createStore(store);
            return "redirect:/web/stores/get-all";
        } catch (HttpStatusCodeException e) {
            model.addAttribute("errorMessage", extractErrorMessage(e));
            model.addAttribute("formTitle", "Create New Store");
            model.addAttribute("actionUrl", "/web/stores/create/save");
            model.addAttribute("isUpdate", false);
            return "stores/store-form";
        }
    }

    @GetMapping("/get-by-id")
    public String showIdRequest(Model model) {
        model.addAttribute("formTitle", "Fetch Store by ID");
        model.addAttribute("actionUrl", "/web/stores/get-by-id/result");
        return "stores/store-id-request";
    }

    @GetMapping("/get-by-id/result")
    public String getByIdResult(@RequestParam("id") String id, Model model, RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("store", storeClient.getStoreById(id));
            return "stores/store-details";
        } catch (HttpStatusCodeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", extractErrorMessage(e));
            return "redirect:/web/stores/get-by-id";
        }
    }

    @GetMapping("/update")
    public String showUpdateIdRequest(Model model) {
        model.addAttribute("formTitle", "Update Store (PUT)");
        model.addAttribute("actionUrl", "/web/stores/update/form");
        return "stores/store-id-request";
    }

    @GetMapping("/update/form")
    public String showUpdateForm(@RequestParam("id") String id, Model model, RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("store", storeClient.getStoreById(id));
            model.addAttribute("formTitle", "Update Store Data");
            model.addAttribute("actionUrl", "/web/stores/update/save");
            model.addAttribute("isUpdate", true);
            return "stores/store-form";
        } catch (HttpStatusCodeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", extractErrorMessage(e));
            return "redirect:/web/stores/update";
        }
    }

    @PostMapping("/update/save")
    public String saveUpdate(@Valid @ModelAttribute("store") StoreDto store, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("formTitle", "Update Store Data");
            model.addAttribute("actionUrl", "/web/stores/update/save");
            model.addAttribute("isUpdate", true);
            return "stores/store-form";
        }
        try {
            storeClient.updateStore(store.getStorId(), store);
            return "redirect:/web/stores/get-all";
        } catch (HttpStatusCodeException e) {
            model.addAttribute("errorMessage", extractErrorMessage(e));
            model.addAttribute("formTitle", "Update Store Data");
            model.addAttribute("actionUrl", "/web/stores/update/save");
            model.addAttribute("isUpdate", true);
            return "stores/store-form";
        }
    }

    @GetMapping("/patch")
    public String showPatchIdRequest(Model model) {
        model.addAttribute("formTitle", "Patch Store");
        model.addAttribute("actionUrl", "/web/stores/patch/form");
        return "stores/store-id-request";
    }

    @GetMapping("/patch/form")
    public String showPatchForm(@RequestParam("id") String id, Model model, RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("currentStore", storeClient.getStoreById(id));
            
            StoreDto patchDto = new StoreDto();
            patchDto.setStorId(id);
            
            model.addAttribute("store", patchDto);
            model.addAttribute("formTitle", "Patch Store Data");
            model.addAttribute("actionUrl", "/web/stores/patch/save");
            return "stores/store-patch";
        } catch (HttpStatusCodeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", extractErrorMessage(e));
            return "redirect:/web/stores/patch";
        }
    }

    @PostMapping("/patch/save")
    public String savePatch(@ModelAttribute("store") StoreDto store, RedirectAttributes redirectAttributes) {
        // Sanitization
        if (store.getStorName() != null && store.getStorName().trim().isEmpty()) store.setStorName(null);
        if (store.getStorAddress() != null && store.getStorAddress().trim().isEmpty()) store.setStorAddress(null);
        if (store.getCity() != null && store.getCity().trim().isEmpty()) store.setCity(null);
        if (store.getState() != null && store.getState().trim().isEmpty()) store.setState(null);
        if (store.getZip() != null && store.getZip().trim().isEmpty()) store.setZip(null);

        try {
            storeClient.patchStore(store.getStorId(), store);
            return "redirect:/web/stores/get-all";
        } catch (HttpStatusCodeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", extractErrorMessage(e));
            return "redirect:/web/stores/patch/form?id=" + store.getStorId();
        }
    }

    // --- FILTERS ---
    @GetMapping("/filter/city")
    public String filterCity(Model model) {
        model.addAttribute("formTitle", "Filter by City");
        model.addAttribute("actionUrl", "/web/stores/filter/city/result");
        model.addAttribute("paramName", "city");
        return "stores/store-single-param-request";
    }

    @GetMapping("/filter/city/result")
    public String filterCityResult(
            @RequestParam("city") String city, 
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "sortBy", defaultValue = "storId") String sortBy,
            @RequestParam(value = "dir", defaultValue = "asc") String dir,
            Model model) {
        List<StoreDto> list = storeClient.filterStoresByCity(city);
        sortStores(list, sortBy, dir);

        model.addAttribute("stores", paginateList(list, page, PAGE_SIZE, model));
        model.addAttribute("pageTitle", "Stores in City: " + city);
        model.addAttribute("endpoint", "/web/stores/filter/city/result");
        model.addAttribute("city", city);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("dir", dir);
        return "stores/store-list";
    }

    @GetMapping("/filter/state")
    public String filterState(Model model) {
        model.addAttribute("formTitle", "Filter by State");
        model.addAttribute("actionUrl", "/web/stores/filter/state/result");
        model.addAttribute("paramName", "state");
        return "stores/store-single-param-request";
    }

    @GetMapping("/filter/state/result")
    public String filterStateResult(
            @RequestParam("state") String state, 
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "sortBy", defaultValue = "storId") String sortBy,
            @RequestParam(value = "dir", defaultValue = "asc") String dir,
            Model model) {
        List<StoreDto> list = storeClient.filterStoresByState(state);
        sortStores(list, sortBy, dir);

        model.addAttribute("stores", paginateList(list, page, PAGE_SIZE, model));
        model.addAttribute("pageTitle", "Stores in State: " + state);
        model.addAttribute("endpoint", "/web/stores/filter/state/result");
        model.addAttribute("state", state);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("dir", dir);
        return "stores/store-list";
    }

    // --- RELATIONAL ---
    @GetMapping("/relational/sales")
    public String relSales(Model model) {
        model.addAttribute("formTitle", "Get Sales by Store");
        model.addAttribute("actionUrl", "/web/stores/relational/sales/result");
        return "stores/store-id-request";
    }

    @GetMapping("/relational/sales/result")
    public String relSalesResult(
            @RequestParam("id") String id, 
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "sortBy", defaultValue = "ordNum") String sortBy,
            @RequestParam(value = "dir", defaultValue = "asc") String dir,
            Model model, RedirectAttributes redirectAttributes) {
        try {
            storeClient.getStoreById(id); // Validate Store
            List<SalesDto> list = storeClient.getSalesByStore(id);
            sortSales(list, sortBy, dir);

            model.addAttribute("targetId", id);
            model.addAttribute("sales", paginateList(list, page, PAGE_SIZE, model));
            model.addAttribute("endpoint", "/web/stores/relational/sales/result");
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("dir", dir);
            return "stores/store-sales";
        } catch (HttpStatusCodeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", extractErrorMessage(e));
            return "redirect:/web/stores/relational/sales";
        }
    }

    @GetMapping("/relational/titles")
    public String relTitles(Model model) {
        model.addAttribute("formTitle", "Get Titles Sold by Store");
        model.addAttribute("actionUrl", "/web/stores/relational/titles/result");
        return "stores/store-id-request";
    }

    @GetMapping("/relational/titles/result")
    public String relTitlesResult(
            @RequestParam("id") String id, 
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "sortBy", defaultValue = "titleId") String sortBy,
            @RequestParam(value = "dir", defaultValue = "asc") String dir,
            Model model, RedirectAttributes redirectAttributes) {
        try {
            storeClient.getStoreById(id); // Validate Store
            List<TitleDto> list = storeClient.getTitlesByStore(id);
            sortTitles(list, sortBy, dir);

            model.addAttribute("targetId", id);
            model.addAttribute("titles", paginateList(list, page, PAGE_SIZE, model));
            model.addAttribute("endpoint", "/web/stores/relational/titles/result");
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("dir", dir);
            return "stores/store-titles";
        } catch (HttpStatusCodeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", extractErrorMessage(e));
            return "redirect:/web/stores/relational/titles";
        }
    }

    @GetMapping("/relational/publishers")
    public String relPublishers(Model model) {
        model.addAttribute("formTitle", "Get Publishers by Store");
        model.addAttribute("actionUrl", "/web/stores/relational/publishers/result");
        return "stores/store-id-request";
    }

    @GetMapping("/relational/publishers/result")
    public String relPublishersResult(
            @RequestParam("id") String id, 
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "sortBy", defaultValue = "pubId") String sortBy,
            @RequestParam(value = "dir", defaultValue = "asc") String dir,
            Model model, RedirectAttributes redirectAttributes) {
        try {
            storeClient.getStoreById(id); // Validate Store
            List<PublisherDto> list = storeClient.getPublishersByStore(id);
            sortPublishers(list, sortBy, dir);

            model.addAttribute("targetId", id);
            model.addAttribute("publishers", paginateList(list, page, PAGE_SIZE, model));
            model.addAttribute("endpoint", "/web/stores/relational/publishers/result");
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("dir", dir);
            return "stores/store-publishers";
        } catch (HttpStatusCodeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", extractErrorMessage(e));
            return "redirect:/web/stores/relational/publishers";
        }
    }

    @GetMapping("/relational/authors")
    public String relAuthors(Model model) {
        model.addAttribute("formTitle", "Get Authors by Store");
        model.addAttribute("actionUrl", "/web/stores/relational/authors/result");
        return "stores/store-id-request";
    }

    @GetMapping("/relational/authors/result")
    public String relAuthorsResult(
            @RequestParam("id") String id, 
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "sortBy", defaultValue = "auId") String sortBy,
            @RequestParam(value = "dir", defaultValue = "asc") String dir,
            Model model, RedirectAttributes redirectAttributes) {
        try {
            storeClient.getStoreById(id); // Validate Store
            List<AuthorDto> list = storeClient.getAuthorsByStore(id);
            sortAuthors(list, sortBy, dir);

            model.addAttribute("targetId", id);
            model.addAttribute("authors", paginateList(list, page, PAGE_SIZE, model));
            model.addAttribute("endpoint", "/web/stores/relational/authors/result");
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("dir", dir);
            return "stores/store-authors";
        } catch (HttpStatusCodeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", extractErrorMessage(e));
            return "redirect:/web/stores/relational/authors";
        }
    }

    @GetMapping("/relational/discounts")
    public String relDiscounts(Model model) {
        model.addAttribute("formTitle", "Get Discounts for Store");
        model.addAttribute("actionUrl", "/web/stores/relational/discounts/result");
        return "stores/store-id-request";
    }

    @GetMapping("/relational/discounts/result")
    public String relDiscountsResult(
            @RequestParam("id") String id, 
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "sortBy", defaultValue = "discountType") String sortBy,
            @RequestParam(value = "dir", defaultValue = "asc") String dir,
            Model model, RedirectAttributes redirectAttributes) {
        try {
            storeClient.getStoreById(id); // Validate Store
            List<DiscountDto> list = storeClient.getDiscountsByStore(id);
            sortDiscounts(list, sortBy, dir);

            model.addAttribute("targetId", id);
            model.addAttribute("discounts", paginateList(list, page, PAGE_SIZE, model));
            model.addAttribute("endpoint", "/web/stores/relational/discounts/result");
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("dir", dir);
            return "stores/store-discounts";
        } catch (HttpStatusCodeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", extractErrorMessage(e));
            return "redirect:/web/stores/relational/discounts";
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

    private void sortStores(List<StoreDto> list, String sortBy, String dir) {
        if ("storName".equals(sortBy)) list.sort(Comparator.comparing(StoreDto::getStorName, Comparator.nullsLast(String::compareToIgnoreCase)));
        else if ("city".equals(sortBy)) list.sort(Comparator.comparing(StoreDto::getCity, Comparator.nullsLast(String::compareToIgnoreCase)));
        else if ("state".equals(sortBy)) list.sort(Comparator.comparing(StoreDto::getState, Comparator.nullsLast(String::compareToIgnoreCase)));
        else if ("zip".equals(sortBy)) list.sort(Comparator.comparing(StoreDto::getZip, Comparator.nullsLast(String::compareToIgnoreCase)));
        else list.sort(Comparator.comparing(StoreDto::getStorId));
        if ("desc".equals(dir)) Collections.reverse(list);
    }

    private void sortSales(List<SalesDto> list, String sortBy, String dir) {
        if ("ordDate".equals(sortBy)) list.sort(Comparator.comparing(SalesDto::getOrdDate, Comparator.nullsLast(Comparator.naturalOrder())));
        else if ("qty".equals(sortBy)) list.sort(Comparator.comparing(SalesDto::getQty, Comparator.nullsLast(Comparator.naturalOrder())));
        else if ("payterms".equals(sortBy)) list.sort(Comparator.comparing(SalesDto::getPayterms, Comparator.nullsLast(String::compareToIgnoreCase)));
        else list.sort(Comparator.comparing(SalesDto::getOrdNum));
        if ("desc".equals(dir)) Collections.reverse(list);
    }

    private void sortTitles(List<TitleDto> list, String sortBy, String dir) {
        if ("titleName".equals(sortBy)) list.sort(Comparator.comparing(TitleDto::getTitleName, Comparator.nullsLast(String::compareToIgnoreCase)));
        else if ("type".equals(sortBy)) list.sort(Comparator.comparing(TitleDto::getType, Comparator.nullsLast(String::compareToIgnoreCase)));
        else if ("price".equals(sortBy)) list.sort(Comparator.comparing(TitleDto::getPrice, Comparator.nullsLast(Comparator.naturalOrder())));
        else list.sort(Comparator.comparing(TitleDto::getTitleId));
        if ("desc".equals(dir)) Collections.reverse(list);
    }

    private void sortPublishers(List<PublisherDto> list, String sortBy, String dir) {
        if ("pubName".equals(sortBy)) list.sort(Comparator.comparing(PublisherDto::getPubName, Comparator.nullsLast(String::compareToIgnoreCase)));
        else if ("city".equals(sortBy)) list.sort(Comparator.comparing(PublisherDto::getCity, Comparator.nullsLast(String::compareToIgnoreCase)));
        else list.sort(Comparator.comparing(PublisherDto::getPubId));
        if ("desc".equals(dir)) Collections.reverse(list);
    }

    private void sortAuthors(List<AuthorDto> list, String sortBy, String dir) {
        if ("auFname".equals(sortBy)) list.sort(Comparator.comparing(AuthorDto::getAuFname, Comparator.nullsLast(String::compareToIgnoreCase)));
        else if ("auLname".equals(sortBy)) list.sort(Comparator.comparing(AuthorDto::getAuLname, Comparator.nullsLast(String::compareToIgnoreCase)));
        else list.sort(Comparator.comparing(AuthorDto::getAuId));
        if ("desc".equals(dir)) Collections.reverse(list);
    }

    private void sortDiscounts(List<DiscountDto> list, String sortBy, String dir) {
        if ("lowQty".equals(sortBy)) list.sort(Comparator.comparing(DiscountDto::getLowQty, Comparator.nullsLast(Comparator.naturalOrder())));
        else if ("highQty".equals(sortBy)) list.sort(Comparator.comparing(DiscountDto::getHighQty, Comparator.nullsLast(Comparator.naturalOrder())));
        else if ("discountAmount".equals(sortBy)) list.sort(Comparator.comparing(DiscountDto::getDiscountAmount, Comparator.nullsLast(Comparator.naturalOrder())));
        else list.sort(Comparator.comparing(DiscountDto::getDiscountType));
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