package com.capg.portal.frontend.controller;

import com.capg.portal.frontend.client.DiscountClient;
import com.capg.portal.frontend.dto.DiscountDto;
import com.capg.portal.frontend.dto.StoreDto;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Controller
@RequestMapping("/web/discounts")
public class DiscountMvcController {

    private final DiscountClient discountClient;
    private final int PAGE_SIZE = 5;

    public DiscountMvcController(DiscountClient discountClient) {
        this.discountClient = discountClient;
    }

    @GetMapping("/operations")
    public String showOperationsMenu() {
        return "discounts/discount-operations";
    }

    // --- CRUD ---
    @GetMapping("/get-all")
    public String getAll(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "sortBy", defaultValue = "discountType") String sortBy,
            @RequestParam(value = "dir", defaultValue = "asc") String dir,
            Model model) {
        
        List<DiscountDto> list = discountClient.getAllDiscounts();
        sortDiscounts(list, sortBy, dir);

        model.addAttribute("discounts", paginateList(list, page, PAGE_SIZE, model));
        model.addAttribute("pageTitle", "All Active Discounts");
        model.addAttribute("endpoint", "/web/discounts/get-all");
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("dir", dir);
        return "discounts/discount-list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        DiscountDto dto = new DiscountDto();
        dto.setStore(new StoreDto()); // Prevents Thymeleaf null reference errors
        model.addAttribute("discount", dto);
        model.addAttribute("formTitle", "Create New Discount");
        model.addAttribute("actionUrl", "/web/discounts/create/save");
        model.addAttribute("isUpdate", false);
        return "discounts/discount-form";
    }

    @PostMapping("/create/save")
    public String saveCreate(@Valid @ModelAttribute("discount") DiscountDto discount, BindingResult result, Model model) {
        // Business Validation
        if (discount.getLowQty() != null && discount.getHighQty() != null && discount.getLowQty() > discount.getHighQty()) {
            result.rejectValue("lowQty", "error.discount", "Low Quantity cannot be greater than High Quantity.");
        }
        if (result.hasErrors()) {
            model.addAttribute("formTitle", "Create New Discount");
            model.addAttribute("actionUrl", "/web/discounts/create/save");
            model.addAttribute("isUpdate", false);
            return "discounts/discount-form";
        }
        
        cleanEmptyStoreId(discount);
        try {
            discountClient.createDiscount(discount);
            return "redirect:/web/discounts/get-all";
        } catch (HttpStatusCodeException e) {
            model.addAttribute("errorMessage", extractErrorMessage(e));
            model.addAttribute("formTitle", "Create New Discount");
            model.addAttribute("actionUrl", "/web/discounts/create/save");
            model.addAttribute("isUpdate", false);
            return "discounts/discount-form";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "An unexpected error occurred: " + e.getMessage());
            model.addAttribute("formTitle", "Create New Discount");
            model.addAttribute("actionUrl", "/web/discounts/create/save");
            model.addAttribute("isUpdate", false);
            return "discounts/discount-form";
        }
    }

    @GetMapping("/get-by-type")
    public String showTypeRequest(Model model) {
        model.addAttribute("formTitle", "Fetch Discount by Type");
        model.addAttribute("actionUrl", "/web/discounts/get-by-type/result");
        return "discounts/discount-type-request";
    }

    @GetMapping("/get-by-type/result")
    public String getTypeResult(@RequestParam("type") String type, Model model, RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("discount", discountClient.getDiscountByType(type));
            return "discounts/discount-details";
        } catch (HttpStatusCodeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", extractErrorMessage(e));
            return "redirect:/web/discounts/get-by-type";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred: " + e.getMessage());
            return "redirect:/web/discounts/get-by-type";
        }
    }

    @GetMapping("/update")
    public String showUpdateTypeRequest(Model model) {
        model.addAttribute("formTitle", "Update Discount (PUT)");
        model.addAttribute("actionUrl", "/web/discounts/update/form");
        return "discounts/discount-type-request";
    }

    @GetMapping("/update/form")
    public String showUpdateForm(@RequestParam("type") String type, Model model, RedirectAttributes redirectAttributes) {
        try {
            DiscountDto discount = discountClient.getDiscountByType(type);
            if (discount.getStore() == null) discount.setStore(new StoreDto()); 
            
            model.addAttribute("discount", discount);
            model.addAttribute("formTitle", "Update Discount Data");
            model.addAttribute("actionUrl", "/web/discounts/update/save");
            model.addAttribute("isUpdate", true);
            return "discounts/discount-form";
        } catch (HttpStatusCodeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", extractErrorMessage(e));
            return "redirect:/web/discounts/update";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred: " + e.getMessage());
            return "redirect:/web/discounts/update";
        }
    }

    @PostMapping("/update/save")
    public String saveUpdate(@Valid @ModelAttribute("discount") DiscountDto discount, BindingResult result, Model model) {
        if (discount.getLowQty() != null && discount.getHighQty() != null && discount.getLowQty() > discount.getHighQty()) {
            result.rejectValue("lowQty", "error.discount", "Low Quantity cannot be greater than High Quantity.");
        }
        if (result.hasErrors()) {
            model.addAttribute("formTitle", "Update Discount Data");
            model.addAttribute("actionUrl", "/web/discounts/update/save");
            model.addAttribute("isUpdate", true);
            return "discounts/discount-form";
        }
        
        cleanEmptyStoreId(discount);
        try {
            discountClient.updateDiscount(discount.getDiscountType(), discount);
            return "redirect:/web/discounts/get-all";
        } catch (HttpStatusCodeException e) {
            model.addAttribute("errorMessage", extractErrorMessage(e));
            model.addAttribute("formTitle", "Update Discount Data");
            model.addAttribute("actionUrl", "/web/discounts/update/save");
            model.addAttribute("isUpdate", true);
            return "discounts/discount-form";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "An unexpected error occurred: " + e.getMessage());
            model.addAttribute("formTitle", "Update Discount Data");
            model.addAttribute("actionUrl", "/web/discounts/update/save");
            model.addAttribute("isUpdate", true);
            return "discounts/discount-form";
        }
    }

    @GetMapping("/patch")
    public String showPatchTypeRequest(Model model) {
        model.addAttribute("formTitle", "Patch Discount");
        model.addAttribute("actionUrl", "/web/discounts/patch/form");
        return "discounts/discount-type-request";
    }

    @GetMapping("/patch/form")
    public String showPatchForm(@RequestParam("type") String type, Model model, RedirectAttributes redirectAttributes) {
        try {

            model.addAttribute("currentDiscount", discountClient.getDiscountByType(type));
            
 
            DiscountDto patchDto = new DiscountDto();
            patchDto.setDiscountType(type);
            patchDto.setStore(new StoreDto()); 
            
            model.addAttribute("discount", patchDto);
            model.addAttribute("formTitle", "Patch Discount Data");
            model.addAttribute("actionUrl", "/web/discounts/patch/save");
            return "discounts/discount-patch";
        } catch (HttpStatusCodeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", extractErrorMessage(e));
            return "redirect:/web/discounts/patch";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred: " + e.getMessage());
            return "redirect:/web/discounts/patch";
        }
    }

    @PostMapping("/patch/save")
    public String savePatch(@ModelAttribute("discount") DiscountDto discount, RedirectAttributes redirectAttributes) {
  
        if (discount.getLowQty() != null && discount.getHighQty() != null && discount.getLowQty() > discount.getHighQty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Validation Error: Low Quantity cannot be greater than High Quantity.");
            return "redirect:/web/discounts/patch/form?type=" + discount.getDiscountType();
        }
        
        cleanEmptyStoreId(discount);
        try {
            discountClient.patchDiscount(discount.getDiscountType(), discount);
            return "redirect:/web/discounts/get-all";
        } catch (HttpStatusCodeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", extractErrorMessage(e));
            return "redirect:/web/discounts/patch/form?type=" + discount.getDiscountType();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred: " + e.getMessage());
            return "redirect:/web/discounts/patch/form?type=" + discount.getDiscountType();
        }
    }


    @GetMapping("/filter/qty")
    public String filterQty() {
        return "discounts/discount-qty-request";
    }

    @GetMapping("/filter/qty/result")
    public String filterQtyResult(
            @RequestParam("minQty") Integer minQty, 
            @RequestParam("maxQty") Integer maxQty, 
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "sortBy", defaultValue = "discountType") String sortBy,
            @RequestParam(value = "dir", defaultValue = "asc") String dir,
            Model model, RedirectAttributes redirectAttributes) {
        try {
            List<DiscountDto> list = discountClient.filterDiscountsByQty(minQty, maxQty);
            sortDiscounts(list, sortBy, dir);

            model.addAttribute("discounts", paginateList(list, page, PAGE_SIZE, model));
            model.addAttribute("pageTitle", "Discounts for Qty " + minQty + " to " + maxQty);
            model.addAttribute("endpoint", "/web/discounts/filter/qty/result");
            model.addAttribute("minQty", minQty);
            model.addAttribute("maxQty", maxQty);
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("dir", dir);
            return "discounts/discount-list";
        } catch (HttpStatusCodeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", extractErrorMessage(e));
            return "redirect:/web/discounts/filter/qty";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred: " + e.getMessage());
            return "redirect:/web/discounts/filter/qty";
        }
    }

    @GetMapping("/filter/amount")
    public String filterAmount() {
        return "discounts/discount-amount-request";
    }

    @GetMapping("/filter/amount/result")
    public String filterAmountResult(
            @RequestParam("maxAmount") BigDecimal maxAmount, 
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "sortBy", defaultValue = "discountType") String sortBy,
            @RequestParam(value = "dir", defaultValue = "asc") String dir,
            Model model, RedirectAttributes redirectAttributes) {
        try {
            List<DiscountDto> list = discountClient.filterDiscountsByAmount(maxAmount);
            sortDiscounts(list, sortBy, dir);

            model.addAttribute("discounts", paginateList(list, page, PAGE_SIZE, model));
            model.addAttribute("pageTitle", "Discounts Less Than " + maxAmount + "%");
            model.addAttribute("endpoint", "/web/discounts/filter/amount/result");
            model.addAttribute("maxAmount", maxAmount);
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("dir", dir);
            return "discounts/discount-list";
        } catch (HttpStatusCodeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", extractErrorMessage(e));
            return "redirect:/web/discounts/filter/amount";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred: " + e.getMessage());
            return "redirect:/web/discounts/filter/amount";
        }
    }

    @GetMapping("/filter/store")
    public String filterStore() {
        return "discounts/discount-store-request"; 
    }

    @GetMapping("/filter/store/result")
    public String filterStoreResult(
            @RequestParam("storId") String storId, 
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "sortBy", defaultValue = "discountType") String sortBy,
            @RequestParam(value = "dir", defaultValue = "asc") String dir,
            Model model, RedirectAttributes redirectAttributes) {
        try {
            List<DiscountDto> list = discountClient.filterDiscountsByStore(storId);
            sortDiscounts(list, sortBy, dir);

            model.addAttribute("discounts", paginateList(list, page, PAGE_SIZE, model));
            model.addAttribute("pageTitle", "Discounts Active at Store " + storId);
            model.addAttribute("endpoint", "/web/discounts/filter/store/result");
            model.addAttribute("storId", storId);
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("dir", dir);
            return "discounts/discount-list";
        } catch (HttpStatusCodeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", extractErrorMessage(e));
            return "redirect:/web/discounts/filter/store";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred: " + e.getMessage());
            return "redirect:/web/discounts/filter/store";
        }
    }


    @GetMapping("/store")
    public String relStoreRequest(Model model) {
        model.addAttribute("formTitle", "Get Store by Discount");
        model.addAttribute("actionUrl", "/web/discounts/store/result");
        return "discounts/discount-type-request";
    }

    @GetMapping("/store/result")
    public String relStoreResult(@RequestParam("type") String type, Model model, RedirectAttributes redirectAttributes) {
        try {
           
            model.addAttribute("targetType", type);
            model.addAttribute("store", discountClient.getStoreByDiscountType(type)); 
            return "discounts/discount-store-result";
        } catch (HttpStatusCodeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", extractErrorMessage(e));
            return "redirect:/web/discounts/store";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred: " + e.getMessage());
            return "redirect:/web/discounts/store";
        }
    }



    private void cleanEmptyStoreId(DiscountDto discount) {
        if (discount.getStore() != null && (discount.getStore().getStorId() == null || discount.getStore().getStorId().trim().isEmpty())) {
            discount.setStore(null);
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

    private void sortDiscounts(List<DiscountDto> list, String sortBy, String dir) {
        if ("lowQty".equals(sortBy)) list.sort(Comparator.comparing(DiscountDto::getLowQty, Comparator.nullsLast(Comparator.naturalOrder())));
        else if ("highQty".equals(sortBy)) list.sort(Comparator.comparing(DiscountDto::getHighQty, Comparator.nullsLast(Comparator.naturalOrder())));
        else if ("discountAmount".equals(sortBy)) list.sort(Comparator.comparing(DiscountDto::getDiscountAmount, Comparator.nullsLast(Comparator.naturalOrder())));
        else if ("storeId".equals(sortBy)) list.sort(Comparator.comparing(d -> d.getStore() != null ? d.getStore().getStorId() : "ZZZZ")); // ZZZZ drops Global to bottom ascending
        else list.sort(Comparator.comparing(DiscountDto::getDiscountType, String::compareToIgnoreCase));
        
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