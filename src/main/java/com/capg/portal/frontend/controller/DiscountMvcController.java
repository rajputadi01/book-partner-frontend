package com.capg.portal.frontend.controller;

import com.capg.portal.frontend.client.DiscountClient;
import com.capg.portal.frontend.dto.DiscountDto;
import com.capg.portal.frontend.dto.StoreDto;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;

@Controller
@RequestMapping("/web/discounts")
public class DiscountMvcController {

    private final DiscountClient discountClient;

    public DiscountMvcController(DiscountClient discountClient) {
        this.discountClient = discountClient;
    }

    @GetMapping("/operations")
    public String showOperationsMenu() {
        return "discounts/discount-operations";
    }

    // --- CRUD ---
    @GetMapping("/get-all")
    public String getAll(Model model) {
        model.addAttribute("discounts", discountClient.getAllDiscounts());
        model.addAttribute("pageTitle", "All Active Discounts");
        return "discounts/discount-list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        DiscountDto dto = new DiscountDto();
        dto.setStore(new StoreDto()); // Prevents Thymeleaf null reference errors on the form
        model.addAttribute("discount", dto);
        model.addAttribute("formTitle", "Create New Discount");
        model.addAttribute("actionUrl", "/web/discounts/create/save");
        model.addAttribute("isUpdate", false);
        return "discounts/discount-form";
    }

    @PostMapping("/create/save")
    public String saveCreate(@Valid @ModelAttribute("discount") DiscountDto discount, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("formTitle", "Create New Discount");
            model.addAttribute("actionUrl", "/web/discounts/create/save");
            model.addAttribute("isUpdate", false);
            return "discounts/discount-form";
        }
        cleanEmptyStoreId(discount);
        discountClient.createDiscount(discount);
        return "redirect:/web/discounts/get-all";
    }

    @GetMapping("/get-by-type")
    public String showTypeRequest(Model model) {
        model.addAttribute("formTitle", "Fetch Discount by Type");
        model.addAttribute("actionUrl", "/web/discounts/get-by-type/result");
        return "discounts/discount-type-request";
    }

    @GetMapping("/get-by-type/result")
    public String getTypeResult(@RequestParam("type") String type, Model model) {
        model.addAttribute("discount", discountClient.getDiscountByType(type));
        return "discounts/discount-details";
    }

    @GetMapping("/update")
    public String showUpdateTypeRequest(Model model) {
        model.addAttribute("formTitle", "Update Discount (PUT)");
        model.addAttribute("actionUrl", "/web/discounts/update/form");
        return "discounts/discount-type-request";
    }

    @GetMapping("/update/form")
    public String showUpdateForm(@RequestParam("type") String type, Model model) {
        DiscountDto discount = discountClient.getDiscountByType(type);
        if (discount.getStore() == null) discount.setStore(new StoreDto()); // Safe form binding
        
        model.addAttribute("discount", discount);
        model.addAttribute("formTitle", "Update Discount Data");
        model.addAttribute("actionUrl", "/web/discounts/update/save");
        model.addAttribute("isUpdate", true);
        return "discounts/discount-form";
    }

    @PostMapping("/update/save")
    public String saveUpdate(@Valid @ModelAttribute("discount") DiscountDto discount, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("formTitle", "Update Discount Data");
            model.addAttribute("actionUrl", "/web/discounts/update/save");
            model.addAttribute("isUpdate", true);
            return "discounts/discount-form";
        }
        cleanEmptyStoreId(discount);
        discountClient.updateDiscount(discount.getDiscountType(), discount);
        return "redirect:/web/discounts/get-all";
    }

    @GetMapping("/patch")
    public String showPatchTypeRequest(Model model) {
        model.addAttribute("formTitle", "Patch Discount");
        model.addAttribute("actionUrl", "/web/discounts/patch/form");
        return "discounts/discount-type-request";
    }

    @GetMapping("/patch/form")
    public String showPatchForm(@RequestParam("type") String type, Model model) {
        DiscountDto discount = discountClient.getDiscountByType(type);
        if (discount.getStore() == null) discount.setStore(new StoreDto());
        
        model.addAttribute("discount", discount);
        model.addAttribute("formTitle", "Patch Discount Data");
        model.addAttribute("actionUrl", "/web/discounts/patch/save");
        model.addAttribute("isUpdate", true);
        return "discounts/discount-form";
    }

    @PostMapping("/patch/save")
    public String savePatch(@ModelAttribute("discount") DiscountDto discount) {
        cleanEmptyStoreId(discount);
        discountClient.patchDiscount(discount.getDiscountType(), discount);
        return "redirect:/web/discounts/get-all";
    }

    // --- FILTERS ---
    @GetMapping("/filter/qty")
    public String filterQty() {
        return "discounts/discount-qty-request";
    }

    @GetMapping("/filter/qty/result")
    public String filterQtyResult(@RequestParam("minQty") Integer minQty, @RequestParam("maxQty") Integer maxQty, Model model) {
        model.addAttribute("discounts", discountClient.filterDiscountsByQty(minQty, maxQty));
        model.addAttribute("pageTitle", "Discounts for Qty " + minQty + " to " + maxQty);
        return "discounts/discount-list";
    }

    @GetMapping("/filter/amount")
    public String filterAmount() {
        return "discounts/discount-amount-request";
    }

    @GetMapping("/filter/amount/result")
    public String filterAmountResult(@RequestParam("maxAmount") BigDecimal maxAmount, Model model) {
        model.addAttribute("discounts", discountClient.filterDiscountsByAmount(maxAmount));
        model.addAttribute("pageTitle", "Discounts Less Than " + maxAmount + "%");
        return "discounts/discount-list";
    }

    @GetMapping("/filter/store")
    public String filterStore() {
        return "discounts/discount-store-request";
    }

    @GetMapping("/filter/store/result")
    public String filterStoreResult(@RequestParam("storId") String storId, Model model) {
        model.addAttribute("discounts", discountClient.filterDiscountsByStore(storId));
        model.addAttribute("pageTitle", "Discounts Active at Store " + storId);
        return "discounts/discount-list";
    }

    // --- RELATIONAL ---
    @GetMapping("/store")
    public String relStoreRequest(Model model) {
        model.addAttribute("formTitle", "Get Store by Discount");
        model.addAttribute("actionUrl", "/web/discounts/store/result");
        return "discounts/discount-type-request"; // Re-uses the type request view
    }

    @GetMapping("/store/result")
    public String relStoreResult(@RequestParam("type") String type, Model model) {
        model.addAttribute("targetType", type);
        // Will return null if backend sent 204 No Content, which thymeleaf handles perfectly!
        model.addAttribute("store", discountClient.getStoreByDiscountType(type)); 
        return "discounts/discount-store-result";
    }

    // UTILITY: Converts an empty string in the Store ID field into a true null for Global Discounts
    private void cleanEmptyStoreId(DiscountDto discount) {
        if (discount.getStore() != null && (discount.getStore().getStorId() == null || discount.getStore().getStorId().trim().isEmpty())) {
            discount.setStore(null);
        }
    }
}