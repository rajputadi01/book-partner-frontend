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

@Controller
@RequestMapping("/web/sales")
public class SalesMvcController {

    private final SalesClient salesClient;

    public SalesMvcController(SalesClient salesClient) {
        this.salesClient = salesClient;
    }

    @GetMapping("/operations")
    public String showOperationsMenu() {
        return "sales/sales-operations";
    }

    // --- CRUD ---
    @GetMapping("/get-all")
    public String getAll(Model model) {
        model.addAttribute("salesList", salesClient.getAllSales());
        model.addAttribute("pageTitle", "All Sales Transactions");
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
        if (result.hasErrors()) {
            model.addAttribute("formTitle", "Record New Sale");
            model.addAttribute("actionUrl", "/web/sales/create/save");
            model.addAttribute("isUpdate", false);
            return "sales/sales-form";
        }
        try {
            salesClient.createSale(sale);
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Conflict: A transaction with this Store ID, Order Number, and Title ID already exists.");
            model.addAttribute("formTitle", "Record New Sale");
            model.addAttribute("actionUrl", "/web/sales/create/save");
            model.addAttribute("isUpdate", false);
            return "sales/sales-form";
        }
        return "redirect:/web/sales/get-all";
    }

    @GetMapping("/get-by-id")
    public String showIdRequest(Model model) {
        model.addAttribute("formTitle", "Fetch Transaction by Triple Key");
        model.addAttribute("actionUrl", "/web/sales/get-by-id/result");
        return "sales/sales-triple-id-request";
    }

    @GetMapping("/get-by-id/result")
    public String getIdResult(@RequestParam("storId") String storId, @RequestParam("ordNum") String ordNum, @RequestParam("titleId") String titleId, Model model) {
        model.addAttribute("sale", salesClient.getSaleById(storId, ordNum, titleId));
        return "sales/sales-details";
    }

    @GetMapping("/update")
    public String showUpdateIdRequest(Model model) {
        model.addAttribute("formTitle", "Update Transaction (PUT)");
        model.addAttribute("actionUrl", "/web/sales/update/form");
        return "sales/sales-triple-id-request";
    }

    @GetMapping("/update/form")
    public String showUpdateForm(@RequestParam("storId") String storId, @RequestParam("ordNum") String ordNum, @RequestParam("titleId") String titleId, Model model) {
        SalesDto sale = salesClient.getSaleById(storId, ordNum, titleId);
        model.addAttribute("sale", sale);
        model.addAttribute("formTitle", "Update Transaction Data");
        model.addAttribute("actionUrl", "/web/sales/update/save");
        model.addAttribute("isUpdate", true);
        return "sales/sales-form";
    }

    @PostMapping("/update/save")
    public String saveUpdate(@Valid @ModelAttribute("sale") SalesDto sale, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("formTitle", "Update Transaction Data");
            model.addAttribute("actionUrl", "/web/sales/update/save");
            model.addAttribute("isUpdate", true);
            return "sales/sales-form";
        }
        salesClient.updateSale(sale.getStore().getStorId(), sale.getOrdNum(), sale.getTitle().getTitleId(), sale);
        return "redirect:/web/sales/get-all";
    }

    @GetMapping("/patch")
    public String showPatchIdRequest(Model model) {
        model.addAttribute("formTitle", "Patch Transaction");
        model.addAttribute("actionUrl", "/web/sales/patch/form");
        return "sales/sales-triple-id-request";
    }

    @GetMapping("/patch/form")
    public String showPatchForm(@RequestParam("storId") String storId, @RequestParam("ordNum") String ordNum, @RequestParam("titleId") String titleId, Model model) {
        SalesDto sale = salesClient.getSaleById(storId, ordNum, titleId);
        model.addAttribute("sale", sale);
        model.addAttribute("formTitle", "Patch Transaction Data");
        model.addAttribute("actionUrl", "/web/sales/patch/save");
        model.addAttribute("isUpdate", true);
        return "sales/sales-form";
    }

    @PostMapping("/patch/save")
    public String savePatch(@ModelAttribute("sale") SalesDto sale) {
        salesClient.patchSale(sale.getStore().getStorId(), sale.getOrdNum(), sale.getTitle().getTitleId(), sale);
        return "redirect:/web/sales/get-all";
    }

    // --- FILTERS ---
    @GetMapping("/filter/store")
    public String filterStore(Model model) {
        model.addAttribute("formTitle", "Transactions by Store ID");
        model.addAttribute("actionUrl", "/web/sales/filter/store/result");
        model.addAttribute("paramName", "storId");
        return "sales/sales-single-param-request";
    }

    @GetMapping("/filter/store/result")
    public String filterStoreResult(@RequestParam("storId") String storId, Model model) {
        model.addAttribute("salesList", salesClient.filterSalesByStore(storId));
        model.addAttribute("pageTitle", "Transactions for Store: " + storId);
        return "sales/sales-list";
    }

    @GetMapping("/filter/title")
    public String filterTitle(Model model) {
        model.addAttribute("formTitle", "Transactions by Title ID");
        model.addAttribute("actionUrl", "/web/sales/filter/title/result");
        model.addAttribute("paramName", "titleId");
        return "sales/sales-single-param-request";
    }

    @GetMapping("/filter/title/result")
    public String filterTitleResult(@RequestParam("titleId") String titleId, Model model) {
        model.addAttribute("salesList", salesClient.filterSalesByTitle(titleId));
        model.addAttribute("pageTitle", "Transactions containing Title: " + titleId);
        return "sales/sales-list";
    }

    @GetMapping("/filter/payterms")
    public String filterPayterms(Model model) {
        model.addAttribute("formTitle", "Transactions by Payment Terms");
        model.addAttribute("actionUrl", "/web/sales/filter/payterms/result");
        model.addAttribute("paramName", "terms");
        return "sales/sales-single-param-request";
    }

    @GetMapping("/filter/payterms/result")
    public String filterPaytermsResult(@RequestParam("terms") String terms, Model model) {
        model.addAttribute("salesList", salesClient.filterSalesByPayterms(terms));
        model.addAttribute("pageTitle", "Transactions with terms: " + terms);
        return "sales/sales-list";
    }

    // --- BI AGGREGATIONS ---
    @GetMapping("/bi/total-qty/store")
    public String biTotalQtyStore(Model model) {
        model.addAttribute("formTitle", "Total Volume (Qty) by Store");
        model.addAttribute("actionUrl", "/web/sales/bi/total-qty/store/result");
        model.addAttribute("paramName", "storId");
        return "sales/sales-single-param-request";
    }

    @GetMapping("/bi/total-qty/store/result")
    public String biTotalQtyStoreResult(@RequestParam("storId") String storId, Model model) {
        model.addAttribute("biTitle", "Total Books Sold by Store");
        model.addAttribute("biDescription", "The total raw volume of individual books purchased by Store ID: " + storId);
        model.addAttribute("biResult", salesClient.getTotalQtyByStore(storId));
        return "sales/sales-bi-result";
    }

    @GetMapping("/bi/count/store")
    public String biCountStore(Model model) {
        model.addAttribute("formTitle", "Total Order Count by Store");
        model.addAttribute("actionUrl", "/web/sales/bi/count/store/result");
        model.addAttribute("paramName", "storId");
        return "sales/sales-single-param-request";
    }

    @GetMapping("/bi/count/store/result")
    public String biCountStoreResult(@RequestParam("storId") String storId, Model model) {
        model.addAttribute("biTitle", "Order Count for Store");
        model.addAttribute("biDescription", "The total number of individual order transactions placed by Store ID: " + storId);
        model.addAttribute("biResult", salesClient.getTransactionCountByStore(storId));
        return "sales/sales-bi-result";
    }

    @GetMapping("/bi/total-qty/title")
    public String biTotalQtyTitle(Model model) {
        model.addAttribute("formTitle", "Global Sales Volume by Title");
        model.addAttribute("actionUrl", "/web/sales/bi/total-qty/title/result");
        model.addAttribute("paramName", "titleId");
        return "sales/sales-single-param-request";
    }

    @GetMapping("/bi/total-qty/title/result")
    public String biTotalQtyTitleResult(@RequestParam("titleId") String titleId, Model model) {
        model.addAttribute("biTitle", "Global Title Sales Volume");
        model.addAttribute("biDescription", "The global total of how many copies of Title ID '" + titleId + "' have been sold across all stores.");
        model.addAttribute("biResult", salesClient.getTotalQtyByTitle(titleId));
        return "sales/sales-bi-result";
    }
}