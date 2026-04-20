package com.capg.portal.frontend.controller;

import com.capg.portal.frontend.client.SalesClient;
import com.capg.portal.frontend.dto.SalesDto;
import com.capg.portal.frontend.dto.StoreDto;
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
@RequestMapping("/web/sales")
public class SalesMvcController {

    private final SalesClient salesClient;
    private final int PAGE_SIZE = 5;

    public SalesMvcController(SalesClient salesClient) {
        this.salesClient = salesClient;
    }

    @GetMapping("/operations")
    public String showOperationsMenu() {
        return "sales/sales-operations";
    }

    @GetMapping("/get-all")
    public String getAll(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "sortBy", defaultValue = "ordNum") String sortBy,
            @RequestParam(value = "dir", defaultValue = "asc") String dir,
            Model model) {
        
        List<SalesDto> list = salesClient.getAllSales();
        sortSales(list, sortBy, dir);

        model.addAttribute("salesList", paginateList(list, page, PAGE_SIZE, model));
        model.addAttribute("pageTitle", "All Sales Transactions");
        model.addAttribute("endpoint", "/web/sales/get-all");
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("dir", dir);
        return "sales/sales-list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        SalesDto dto = new SalesDto();
        dto.setStore(new StoreDto());
        dto.setTitle(new TitleDto());

        model.addAttribute("sale", dto);
        model.addAttribute("formTitle", "Record New Sale");
        model.addAttribute("actionUrl", "/web/sales/create/save");
        model.addAttribute("isUpdate", false);
        return "sales/sales-form";
    }

    @PostMapping("/create/save")
    public String saveCreate(@Valid @ModelAttribute("sale") SalesDto sale, BindingResult result, Model model) {
        // Pre-Flight Validation
        if (sale.getQty() != null && sale.getQty() < 1) {
            result.rejectValue("qty", "error.sale", "Quantity must be at least 1.");
        }
        
        if (result.hasErrors()) {
            model.addAttribute("formTitle", "Record New Sale");
            model.addAttribute("actionUrl", "/web/sales/create/save");
            model.addAttribute("isUpdate", false);
            return "sales/sales-form";
        }
        
        try {
            salesClient.createSale(sale);
            return "redirect:/web/sales/get-all";
        } catch (HttpStatusCodeException e) {
            model.addAttribute("errorMessage", extractErrorMessage(e));
            model.addAttribute("formTitle", "Record New Sale");
            model.addAttribute("actionUrl", "/web/sales/create/save");
            model.addAttribute("isUpdate", false);
            return "sales/sales-form";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "An unexpected error occurred: " + e.getMessage());
            model.addAttribute("formTitle", "Record New Sale");
            model.addAttribute("actionUrl", "/web/sales/create/save");
            model.addAttribute("isUpdate", false);
            return "sales/sales-form";
        }
    }

    @GetMapping("/get-by-id")
    public String showIdRequest(Model model) {
        model.addAttribute("formTitle", "Fetch Transaction by Triple Key");
        model.addAttribute("actionUrl", "/web/sales/get-by-id/result");
        return "sales/sales-triple-id-request";
    }

    @GetMapping("/get-by-id/result")
    public String getIdResult(
            @RequestParam("storId") String storId, 
            @RequestParam("ordNum") String ordNum, 
            @RequestParam("titleId") String titleId, 
            Model model, RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("sale", salesClient.getSaleById(storId, ordNum, titleId));
            return "sales/sales-details";
        } catch (HttpStatusCodeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", extractErrorMessage(e));
            return "redirect:/web/sales/get-by-id";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred: " + e.getMessage());
            return "redirect:/web/sales/get-by-id";
        }
    }

    @GetMapping("/update")
    public String showUpdateIdRequest(Model model) {
        model.addAttribute("formTitle", "Update Transaction (PUT)");
        model.addAttribute("actionUrl", "/web/sales/update/form");
        return "sales/sales-triple-id-request";
    }

    @GetMapping("/update/form")
    public String showUpdateForm(
            @RequestParam("storId") String storId, 
            @RequestParam("ordNum") String ordNum, 
            @RequestParam("titleId") String titleId, 
            Model model, RedirectAttributes redirectAttributes) {
        try {
            SalesDto sale = salesClient.getSaleById(storId, ordNum, titleId);
            model.addAttribute("sale", sale);
            model.addAttribute("formTitle", "Update Transaction Data");
            model.addAttribute("actionUrl", "/web/sales/update/save");
            model.addAttribute("isUpdate", true);
            return "sales/sales-form";
        } catch (HttpStatusCodeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", extractErrorMessage(e));
            return "redirect:/web/sales/update";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred: " + e.getMessage());
            return "redirect:/web/sales/update";
        }
    }

    @PostMapping("/update/save")
    public String saveUpdate(@Valid @ModelAttribute("sale") SalesDto sale, BindingResult result, Model model) {
        if (sale.getQty() != null && sale.getQty() < 1) {
            result.rejectValue("qty", "error.sale", "Quantity must be at least 1.");
        }
        
        if (result.hasErrors()) {
            model.addAttribute("formTitle", "Update Transaction Data");
            model.addAttribute("actionUrl", "/web/sales/update/save");
            model.addAttribute("isUpdate", true);
            return "sales/sales-form";
        }
        
        try {
            salesClient.updateSale(sale.getStore().getStorId(), sale.getOrdNum(), sale.getTitle().getTitleId(), sale);
            return "redirect:/web/sales/get-all";
        } catch (HttpStatusCodeException e) {
            model.addAttribute("errorMessage", extractErrorMessage(e));
            model.addAttribute("formTitle", "Update Transaction Data");
            model.addAttribute("actionUrl", "/web/sales/update/save");
            model.addAttribute("isUpdate", true);
            return "sales/sales-form";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "An unexpected error occurred: " + e.getMessage());
            model.addAttribute("formTitle", "Update Transaction Data");
            model.addAttribute("actionUrl", "/web/sales/update/save");
            model.addAttribute("isUpdate", true);
            return "sales/sales-form";
        }
    }

    @GetMapping("/patch")
    public String showPatchIdRequest(Model model) {
        model.addAttribute("formTitle", "Patch Transaction");
        model.addAttribute("actionUrl", "/web/sales/patch/form");
        return "sales/sales-triple-id-request";
    }

    @GetMapping("/patch/form")
    public String showPatchForm(
            @RequestParam("storId") String storId, 
            @RequestParam("ordNum") String ordNum, 
            @RequestParam("titleId") String titleId, 
            Model model, RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("currentRecord", salesClient.getSaleById(storId, ordNum, titleId));
            
            SalesDto patchDto = new SalesDto();
            patchDto.setStore(new StoreDto());
            patchDto.getStore().setStorId(storId);
            patchDto.setOrdNum(ordNum);
            patchDto.setTitle(new TitleDto());
            patchDto.getTitle().setTitleId(titleId);
            
            model.addAttribute("sale", patchDto);
            model.addAttribute("formTitle", "Patch Transaction Data");
            model.addAttribute("actionUrl", "/web/sales/patch/save");
            return "sales/sales-patch";
        } catch (HttpStatusCodeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", extractErrorMessage(e));
            return "redirect:/web/sales/patch";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred: " + e.getMessage());
            return "redirect:/web/sales/patch";
        }
    }

    @PostMapping("/patch/save")
    public String savePatch(@ModelAttribute("sale") SalesDto sale, RedirectAttributes redirectAttributes) {
        // Sanitization
        if (sale.getPayterms() != null && sale.getPayterms().trim().isEmpty()) sale.setPayterms(null);
        
        if (sale.getQty() != null && sale.getQty() < 1) {
            redirectAttributes.addFlashAttribute("errorMessage", "Validation Error: Quantity must be at least 1.");
            return "redirect:/web/sales/patch/form?storId=" + sale.getStore().getStorId() + "&ordNum=" + sale.getOrdNum() + "&titleId=" + sale.getTitle().getTitleId();
        }

        try {
            salesClient.patchSale(sale.getStore().getStorId(), sale.getOrdNum(), sale.getTitle().getTitleId(), sale);
            return "redirect:/web/sales/get-all";
        } catch (HttpStatusCodeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", extractErrorMessage(e));
            return "redirect:/web/sales/patch/form?storId=" + sale.getStore().getStorId() + "&ordNum=" + sale.getOrdNum() + "&titleId=" + sale.getTitle().getTitleId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred: " + e.getMessage());
            return "redirect:/web/sales/patch/form?storId=" + sale.getStore().getStorId() + "&ordNum=" + sale.getOrdNum() + "&titleId=" + sale.getTitle().getTitleId();
        }
    }

    @GetMapping("/filter/store")
    public String filterStore(Model model) {
        model.addAttribute("formTitle", "Transactions by Store ID");
        model.addAttribute("actionUrl", "/web/sales/filter/store/result");
        model.addAttribute("paramName", "storId");
        return "sales/sales-single-param-request";
    }

    @GetMapping("/filter/store/result")
    public String filterStoreResult(
            @RequestParam("storId") String storId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "sortBy", defaultValue = "ordNum") String sortBy,
            @RequestParam(value = "dir", defaultValue = "asc") String dir,
            Model model, RedirectAttributes redirectAttributes) {
        try {
            List<SalesDto> list = salesClient.filterSalesByStore(storId);
            sortSales(list, sortBy, dir);

            model.addAttribute("salesList", paginateList(list, page, PAGE_SIZE, model));
            model.addAttribute("pageTitle", "Transactions for Store: " + storId);
            model.addAttribute("endpoint", "/web/sales/filter/store/result");
            model.addAttribute("storId", storId);
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("dir", dir);
            return "sales/sales-list";
        } catch (HttpStatusCodeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", extractErrorMessage(e));
            return "redirect:/web/sales/filter/store";
        }
    }

    @GetMapping("/filter/title")
    public String filterTitle(Model model) {
        model.addAttribute("formTitle", "Transactions by Title ID");
        model.addAttribute("actionUrl", "/web/sales/filter/title/result");
        model.addAttribute("paramName", "titleId");
        return "sales/sales-single-param-request";
    }

    @GetMapping("/filter/title/result")
    public String filterTitleResult(
            @RequestParam("titleId") String titleId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "sortBy", defaultValue = "ordNum") String sortBy,
            @RequestParam(value = "dir", defaultValue = "asc") String dir,
            Model model, RedirectAttributes redirectAttributes) {
        try {
            List<SalesDto> list = salesClient.filterSalesByTitle(titleId);
            sortSales(list, sortBy, dir);

            model.addAttribute("salesList", paginateList(list, page, PAGE_SIZE, model));
            model.addAttribute("pageTitle", "Transactions containing Title: " + titleId);
            model.addAttribute("endpoint", "/web/sales/filter/title/result");
            model.addAttribute("titleId", titleId);
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("dir", dir);
            return "sales/sales-list";
        } catch (HttpStatusCodeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", extractErrorMessage(e));
            return "redirect:/web/sales/filter/title";
        }
    }

    @GetMapping("/filter/payterms")
    public String filterPayterms(Model model) {
        model.addAttribute("formTitle", "Transactions by Payment Terms");
        model.addAttribute("actionUrl", "/web/sales/filter/payterms/result");
        model.addAttribute("paramName", "terms");
        return "sales/sales-single-param-request";
    }

    @GetMapping("/filter/payterms/result")
    public String filterPaytermsResult(
            @RequestParam("terms") String terms,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "sortBy", defaultValue = "ordNum") String sortBy,
            @RequestParam(value = "dir", defaultValue = "asc") String dir,
            Model model, RedirectAttributes redirectAttributes) {
        try {
            List<SalesDto> list = salesClient.filterSalesByPayterms(terms);
            sortSales(list, sortBy, dir);

            model.addAttribute("salesList", paginateList(list, page, PAGE_SIZE, model));
            model.addAttribute("pageTitle", "Transactions with terms: " + terms);
            model.addAttribute("endpoint", "/web/sales/filter/payterms/result");
            model.addAttribute("terms", terms);
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("dir", dir);
            return "sales/sales-list";
        } catch (HttpStatusCodeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", extractErrorMessage(e));
            return "redirect:/web/sales/filter/payterms";
        }
    }

    @GetMapping("/bi/total-qty/store")
    public String biTotalQtyStore(Model model) {
        model.addAttribute("formTitle", "Total Volume (Qty) by Store");
        model.addAttribute("actionUrl", "/web/sales/bi/total-qty/store/result");
        model.addAttribute("paramName", "storId");
        return "sales/sales-single-param-request";
    }

    @GetMapping("/bi/total-qty/store/result")
    public String biTotalQtyStoreResult(@RequestParam("storId") String storId, Model model, RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("biTitle", "Total Books Sold by Store");
            model.addAttribute("biDescription", "The total raw volume of individual books purchased by Store ID: " + storId);
            model.addAttribute("biResult", salesClient.getTotalQtyByStore(storId));
            return "sales/sales-bi-result";
        } catch (HttpStatusCodeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", extractErrorMessage(e));
            return "redirect:/web/sales/bi/total-qty/store";
        }
    }

    @GetMapping("/bi/count/store")
    public String biCountStore(Model model) {
        model.addAttribute("formTitle", "Total Order Count by Store");
        model.addAttribute("actionUrl", "/web/sales/bi/count/store/result");
        model.addAttribute("paramName", "storId");
        return "sales/sales-single-param-request";
    }

    @GetMapping("/bi/count/store/result")
    public String biCountStoreResult(@RequestParam("storId") String storId, Model model, RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("biTitle", "Order Count for Store");
            model.addAttribute("biDescription", "The total number of individual order transactions placed by Store ID: " + storId);
            model.addAttribute("biResult", salesClient.getTransactionCountByStore(storId));
            return "sales/sales-bi-result";
        } catch (HttpStatusCodeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", extractErrorMessage(e));
            return "redirect:/web/sales/bi/count/store";
        }
    }

    @GetMapping("/bi/total-qty/title")
    public String biTotalQtyTitle(Model model) {
        model.addAttribute("formTitle", "Global Sales Volume by Title");
        model.addAttribute("actionUrl", "/web/sales/bi/total-qty/title/result");
        model.addAttribute("paramName", "titleId");
        return "sales/sales-single-param-request";
    }

    @GetMapping("/bi/total-qty/title/result")
    public String biTotalQtyTitleResult(@RequestParam("titleId") String titleId, Model model, RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("biTitle", "Global Title Sales Volume");
            model.addAttribute("biDescription", "The global total of how many copies of Title ID '" + titleId + "' have been sold across all stores.");
            model.addAttribute("biResult", salesClient.getTotalQtyByTitle(titleId));
            return "sales/sales-bi-result";
        } catch (HttpStatusCodeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", extractErrorMessage(e));
            return "redirect:/web/sales/bi/total-qty/title";
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

    private void sortSales(List<SalesDto> list, String sortBy, String dir) {
        if ("storId".equals(sortBy)) list.sort(Comparator.comparing(s -> s.getStore() != null ? s.getStore().getStorId() : "", Comparator.nullsLast(String::compareToIgnoreCase)));
        else if ("titleId".equals(sortBy)) list.sort(Comparator.comparing(s -> s.getTitle() != null ? s.getTitle().getTitleId() : "", Comparator.nullsLast(String::compareToIgnoreCase)));
        else if ("ordDate".equals(sortBy)) list.sort(Comparator.comparing(SalesDto::getOrdDate, Comparator.nullsLast(Comparator.naturalOrder())));
        else if ("qty".equals(sortBy)) list.sort(Comparator.comparing(SalesDto::getQty, Comparator.nullsLast(Comparator.naturalOrder())));
        else if ("payterms".equals(sortBy)) list.sort(Comparator.comparing(SalesDto::getPayterms, Comparator.nullsLast(String::compareToIgnoreCase)));
        else list.sort(Comparator.comparing(SalesDto::getOrdNum, Comparator.nullsLast(String::compareToIgnoreCase)));
        
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