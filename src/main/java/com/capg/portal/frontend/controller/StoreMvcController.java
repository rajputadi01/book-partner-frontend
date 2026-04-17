package com.capg.portal.frontend.controller;

import com.capg.portal.frontend.client.StoreClient;
import com.capg.portal.frontend.dto.StoreDto;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/web/stores")
public class StoreMvcController {

    private final StoreClient storeClient;

    public StoreMvcController(StoreClient storeClient) {
        this.storeClient = storeClient;
    }

    @GetMapping("/operations")
    public String showOperationsMenu() {
        return "stores/store-operations";
    }

    // --- CRUD ---
    @GetMapping("/get-all")
    public String getAllStores(Model model) {
        model.addAttribute("stores", storeClient.getAllStores());
        model.addAttribute("pageTitle", "All Store Records");
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
        storeClient.createStore(store);
        return "redirect:/web/stores/get-all";
    }

    @GetMapping("/get-by-id")
    public String showIdRequest(Model model) {
        model.addAttribute("formTitle", "Fetch Store by ID");
        model.addAttribute("actionUrl", "/web/stores/get-by-id/result");
        return "stores/store-id-request";
    }

    @GetMapping("/get-by-id/result")
    public String getByIdResult(@RequestParam("id") String id, Model model) {
        model.addAttribute("store", storeClient.getStoreById(id));
        return "stores/store-details";
    }

    @GetMapping("/update")
    public String showUpdateIdRequest(Model model) {
        model.addAttribute("formTitle", "Update Store (PUT)");
        model.addAttribute("actionUrl", "/web/stores/update/form");
        return "stores/store-id-request";
    }

    @GetMapping("/update/form")
    public String showUpdateForm(@RequestParam("id") String id, Model model) {
        model.addAttribute("store", storeClient.getStoreById(id));
        model.addAttribute("formTitle", "Update Store Data");
        model.addAttribute("actionUrl", "/web/stores/update/save");
        model.addAttribute("isUpdate", true);
        return "stores/store-form";
    }

    @PostMapping("/update/save")
    public String saveUpdate(@Valid @ModelAttribute("store") StoreDto store, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("formTitle", "Update Store Data");
            model.addAttribute("actionUrl", "/web/stores/update/save");
            model.addAttribute("isUpdate", true);
            return "stores/store-form";
        }
        storeClient.updateStore(store.getStorId(), store);
        return "redirect:/web/stores/get-all";
    }

    @GetMapping("/patch")
    public String showPatchIdRequest(Model model) {
        model.addAttribute("formTitle", "Patch Store");
        model.addAttribute("actionUrl", "/web/stores/patch/form");
        return "stores/store-id-request";
    }

    @GetMapping("/patch/form")
    public String showPatchForm(@RequestParam("id") String id, Model model) {
        model.addAttribute("store", storeClient.getStoreById(id));
        model.addAttribute("formTitle", "Patch Store Data");
        model.addAttribute("actionUrl", "/web/stores/patch/save");
        model.addAttribute("isUpdate", true);
        return "stores/store-form";
    }

    @PostMapping("/patch/save")
    public String savePatch(@ModelAttribute("store") StoreDto store) {
        storeClient.patchStore(store.getStorId(), store);
        return "redirect:/web/stores/get-all";
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
    public String filterCityResult(@RequestParam("city") String city, Model model) {
        model.addAttribute("stores", storeClient.filterStoresByCity(city));
        model.addAttribute("pageTitle", "Stores in City: " + city);
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
    public String filterStateResult(@RequestParam("state") String state, Model model) {
        model.addAttribute("stores", storeClient.filterStoresByState(state));
        model.addAttribute("pageTitle", "Stores in State: " + state);
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
    public String relSalesResult(@RequestParam("id") String id, Model model) {
        model.addAttribute("targetId", id);
        model.addAttribute("sales", storeClient.getSalesByStore(id));
        return "stores/store-sales";
    }

    @GetMapping("/relational/titles")
    public String relTitles(Model model) {
        model.addAttribute("formTitle", "Get Titles Sold by Store");
        model.addAttribute("actionUrl", "/web/stores/relational/titles/result");
        return "stores/store-id-request";
    }

    @GetMapping("/relational/titles/result")
    public String relTitlesResult(@RequestParam("id") String id, Model model) {
        model.addAttribute("targetId", id);
        model.addAttribute("titles", storeClient.getTitlesByStore(id));
        return "stores/store-titles";
    }

    @GetMapping("/relational/publishers")
    public String relPublishers(Model model) {
        model.addAttribute("formTitle", "Get Publishers by Store");
        model.addAttribute("actionUrl", "/web/stores/relational/publishers/result");
        return "stores/store-id-request";
    }

    @GetMapping("/relational/publishers/result")
    public String relPublishersResult(@RequestParam("id") String id, Model model) {
        model.addAttribute("targetId", id);
        model.addAttribute("publishers", storeClient.getPublishersByStore(id));
        return "stores/store-publishers";
    }

    @GetMapping("/relational/authors")
    public String relAuthors(Model model) {
        model.addAttribute("formTitle", "Get Authors by Store");
        model.addAttribute("actionUrl", "/web/stores/relational/authors/result");
        return "stores/store-id-request";
    }

    @GetMapping("/relational/authors/result")
    public String relAuthorsResult(@RequestParam("id") String id, Model model) {
        model.addAttribute("targetId", id);
        model.addAttribute("authors", storeClient.getAuthorsByStore(id));
        return "stores/store-authors";
    }

    @GetMapping("/relational/discounts")
    public String relDiscounts(Model model) {
        model.addAttribute("formTitle", "Get Discounts for Store");
        model.addAttribute("actionUrl", "/web/stores/relational/discounts/result");
        return "stores/store-id-request";
    }

    @GetMapping("/relational/discounts/result")
    public String relDiscountsResult(@RequestParam("id") String id, Model model) {
        model.addAttribute("targetId", id);
        model.addAttribute("discounts", storeClient.getDiscountsByStore(id));
        return "stores/store-discounts";
    }
}