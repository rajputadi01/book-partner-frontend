package com.capg.portal.frontend.controller;

import com.capg.portal.frontend.client.AuthorClient;
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
@RequestMapping("/web/authors")
public class AuthorMvcController {

    private final AuthorClient authorClient;
    private final int PAGE_SIZE = 5;

    public AuthorMvcController(AuthorClient authorClient) {
        this.authorClient = authorClient;
    }

    @GetMapping("/operations")
    public String showOperationsMenu() {
        return "authors/author-operations";
    }

    @GetMapping("/get-all")
    public String getAllAuthors(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "sortBy", defaultValue = "auId") String sortBy,
            @RequestParam(value = "dir", defaultValue = "asc") String dir,
            Model model) {
        
        List<AuthorDto> list = authorClient.getAllAuthors();
        sortAuthors(list, sortBy, dir);
        
        model.addAttribute("authors", paginateList(list, page, PAGE_SIZE, model));
        model.addAttribute("pageTitle", "All Author Records");
        model.addAttribute("endpoint", "/web/authors/get-all");
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("dir", dir);
        return "authors/author-list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("author", new AuthorDto());
        model.addAttribute("formTitle", "Create New Author");
        model.addAttribute("actionUrl", "/web/authors/create/save");
        model.addAttribute("isUpdate", false);
        return "authors/author-form";
    }

    @PostMapping("/create/save")
    public String saveAuthor(@Valid @ModelAttribute("author") AuthorDto author, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("formTitle", "Create New Author");
            model.addAttribute("actionUrl", "/web/authors/create/save");
            model.addAttribute("isUpdate", false);
            return "authors/author-form";
        }
        try {
            authorClient.createAuthor(author);
            return "redirect:/web/authors/get-all";
        } catch (HttpStatusCodeException e) {
            model.addAttribute("errorMessage", extractErrorMessage(e));
            model.addAttribute("formTitle", "Create New Author");
            model.addAttribute("actionUrl", "/web/authors/create/save");
            model.addAttribute("isUpdate", false);
            return "authors/author-form";
        }
    }

    @GetMapping("/get-by-id")
    public String showIdRequest(Model model) {
        model.addAttribute("formTitle", "Fetch Author by ID");
        model.addAttribute("actionUrl", "/web/authors/get-by-id/result");
        return "authors/author-id-request";
    }

    @GetMapping("/get-by-id/result")
    public String getByIdResult(@RequestParam("id") String id, Model model, RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("author", authorClient.getAuthorById(id));
            return "authors/author-details";
        } catch (HttpStatusCodeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", extractErrorMessage(e));
            return "redirect:/web/authors/get-by-id";
        }
    }

    @GetMapping("/update")
    public String showUpdateIdRequest(Model model) {
        model.addAttribute("formTitle", "Update Author (PUT)");
        model.addAttribute("actionUrl", "/web/authors/update/form");
        return "authors/author-id-request";
    }

    @GetMapping("/update/form")
    public String showUpdateForm(@RequestParam("id") String id, Model model, RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("author", authorClient.getAuthorById(id));
            model.addAttribute("formTitle", "Update Author Data");
            model.addAttribute("actionUrl", "/web/authors/update/save");
            model.addAttribute("isUpdate", true);
            return "authors/author-form";
        } catch (HttpStatusCodeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", extractErrorMessage(e));
            return "redirect:/web/authors/update";
        }
    }

    @PostMapping("/update/save")
    public String saveUpdate(@Valid @ModelAttribute("author") AuthorDto author, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("formTitle", "Update Author Data");
            model.addAttribute("actionUrl", "/web/authors/update/save");
            model.addAttribute("isUpdate", true);
            return "authors/author-form";
        }
        try {
            authorClient.updateAuthor(author.getAuId(), author);
            return "redirect:/web/authors/get-all";
        } catch (HttpStatusCodeException e) {
            model.addAttribute("errorMessage", extractErrorMessage(e));
            model.addAttribute("formTitle", "Update Author Data");
            model.addAttribute("actionUrl", "/web/authors/update/save");
            model.addAttribute("isUpdate", true);
            return "authors/author-form";
        }
    }

    @GetMapping("/patch")
    public String showPatchIdRequest(Model model) {
        model.addAttribute("formTitle", "Patch Author");
        model.addAttribute("actionUrl", "/web/authors/patch/form");
        return "authors/author-id-request";
    }

    @GetMapping("/patch/form")
    public String showPatchForm(@RequestParam("id") String id, Model model, RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("currentAuthor", authorClient.getAuthorById(id));
            
            AuthorDto patchDto = new AuthorDto();
            patchDto.setAuId(id);
            
            model.addAttribute("author", patchDto);
            model.addAttribute("formTitle", "Patch Author Data");
            model.addAttribute("actionUrl", "/web/authors/patch/save");
            return "authors/author-patch";
        } catch (HttpStatusCodeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", extractErrorMessage(e));
            return "redirect:/web/authors/patch";
        }
    }

    @PostMapping("/patch/save")
    public String savePatch(@ModelAttribute("author") AuthorDto author, RedirectAttributes redirectAttributes) {
        if (author.getAuFname() != null && author.getAuFname().trim().isEmpty()) author.setAuFname(null);
        if (author.getAuLname() != null && author.getAuLname().trim().isEmpty()) author.setAuLname(null);
        if (author.getPhone() != null && author.getPhone().trim().isEmpty()) author.setPhone(null);
        if (author.getAddress() != null && author.getAddress().trim().isEmpty()) author.setAddress(null);
        if (author.getCity() != null && author.getCity().trim().isEmpty()) author.setCity(null);
        if (author.getState() != null && author.getState().trim().isEmpty()) author.setState(null);
        if (author.getZip() != null && author.getZip().trim().isEmpty()) author.setZip(null);

        try {
            authorClient.patchAuthor(author.getAuId(), author);
            return "redirect:/web/authors/get-all";
        } catch (HttpStatusCodeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", extractErrorMessage(e));
            return "redirect:/web/authors/patch/form?id=" + author.getAuId();
        }
    }

    @GetMapping("/filter/contract")
    public String filterContract(Model model) {
        model.addAttribute("formTitle", "Filter by Contract Status");
        model.addAttribute("actionUrl", "/web/authors/filter/contract/result");
        model.addAttribute("paramName", "status");
        model.addAttribute("inputType", "number");
        return "authors/author-single-param-request";
    }

    @GetMapping("/filter/contract/result")
    public String filterContractResult(
            @RequestParam("status") Integer status, 
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "sortBy", defaultValue = "auId") String sortBy,
            @RequestParam(value = "dir", defaultValue = "asc") String dir,
            Model model) {
        List<AuthorDto> list = authorClient.filterAuthorsByContract(status);
        sortAuthors(list, sortBy, dir);

        model.addAttribute("authors", paginateList(list, page, PAGE_SIZE, model));
        model.addAttribute("pageTitle", "Authors with Contract Status: " + status);
        model.addAttribute("endpoint", "/web/authors/filter/contract/result");
        model.addAttribute("status", status);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("dir", dir);
        return "authors/author-list";
    }

    @GetMapping("/filter/city")
    public String filterCity(Model model) {
        model.addAttribute("formTitle", "Filter by City");
        model.addAttribute("actionUrl", "/web/authors/filter/city/result");
        model.addAttribute("paramName", "city");
        model.addAttribute("inputType", "text");
        return "authors/author-single-param-request";
    }

    @GetMapping("/filter/city/result")
    public String filterCityResult(
            @RequestParam("city") String city, 
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "sortBy", defaultValue = "auId") String sortBy,
            @RequestParam(value = "dir", defaultValue = "asc") String dir,
            Model model) {
        List<AuthorDto> list = authorClient.filterAuthorsByCity(city);
        sortAuthors(list, sortBy, dir);

        model.addAttribute("authors", paginateList(list, page, PAGE_SIZE, model));
        model.addAttribute("pageTitle", "Authors in City: " + city);
        model.addAttribute("endpoint", "/web/authors/filter/city/result");
        model.addAttribute("city", city);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("dir", dir);
        return "authors/author-list";
    }

    @GetMapping("/filter/state")
    public String filterState(Model model) {
        model.addAttribute("formTitle", "Filter by State");
        model.addAttribute("actionUrl", "/web/authors/filter/state/result");
        model.addAttribute("paramName", "state");
        model.addAttribute("inputType", "text");
        return "authors/author-single-param-request";
    }

    @GetMapping("/filter/state/result")
    public String filterStateResult(
            @RequestParam("state") String state, 
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "sortBy", defaultValue = "auId") String sortBy,
            @RequestParam(value = "dir", defaultValue = "asc") String dir,
            Model model) {
        List<AuthorDto> list = authorClient.filterAuthorsByState(state);
        sortAuthors(list, sortBy, dir);

        model.addAttribute("authors", paginateList(list, page, PAGE_SIZE, model));
        model.addAttribute("pageTitle", "Authors in State: " + state);
        model.addAttribute("endpoint", "/web/authors/filter/state/result");
        model.addAttribute("state", state);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("dir", dir);
        return "authors/author-list";
    }

    @GetMapping("/relational/title-authors")
    public String relTitleAuthors(Model model) {
        model.addAttribute("formTitle", "Get Contracts by Author");
        model.addAttribute("actionUrl", "/web/authors/relational/title-authors/result");
        return "authors/author-id-request";
    }

    @GetMapping("/relational/title-authors/result")
    public String relTitleAuthorsResult(
            @RequestParam("id") String id, 
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "sortBy", defaultValue = "titleId") String sortBy,
            @RequestParam(value = "dir", defaultValue = "asc") String dir,
            Model model, RedirectAttributes redirectAttributes) {
        try {
            authorClient.getAuthorById(id); // Validate Author
            List<TitleAuthorDto> list = authorClient.getTitleAuthorsByAuthor(id);
            sortTitleAuthors(list, sortBy, dir);

            model.addAttribute("targetId", id);
            model.addAttribute("titleAuthors", paginateList(list, page, PAGE_SIZE, model));
            model.addAttribute("endpoint", "/web/authors/relational/title-authors/result");
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("dir", dir);
            return "authors/author-title-authors";
        } catch (HttpStatusCodeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", extractErrorMessage(e));
            return "redirect:/web/authors/relational/title-authors";
        }
    }

    @GetMapping("/relational/titles")
    public String relTitles(Model model) {
        model.addAttribute("formTitle", "Get Titles by Author");
        model.addAttribute("actionUrl", "/web/authors/relational/titles/result");
        return "authors/author-id-request";
    }

    @GetMapping("/relational/titles/result")
    public String relTitlesResult(
            @RequestParam("id") String id, 
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "sortBy", defaultValue = "titleId") String sortBy,
            @RequestParam(value = "dir", defaultValue = "asc") String dir,
            Model model, RedirectAttributes redirectAttributes) {
        try {
            authorClient.getAuthorById(id); // Validate Author
            List<TitleDto> list = authorClient.getTitlesByAuthor(id);
            sortTitles(list, sortBy, dir);

            model.addAttribute("targetId", id);
            model.addAttribute("titles", paginateList(list, page, PAGE_SIZE, model));
            model.addAttribute("endpoint", "/web/authors/relational/titles/result");
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("dir", dir);
            return "authors/author-titles";
        } catch (HttpStatusCodeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", extractErrorMessage(e));
            return "redirect:/web/authors/relational/titles";
        }
    }

    @GetMapping("/relational/publishers")
    public String relPublishers(Model model) {
        model.addAttribute("formTitle", "Get Publishers by Author");
        model.addAttribute("actionUrl", "/web/authors/relational/publishers/result");
        return "authors/author-id-request";
    }

    @GetMapping("/relational/publishers/result")
    public String relPublishersResult(
            @RequestParam("id") String id, 
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "sortBy", defaultValue = "pubId") String sortBy,
            @RequestParam(value = "dir", defaultValue = "asc") String dir,
            Model model, RedirectAttributes redirectAttributes) {
        try {
            authorClient.getAuthorById(id); // Validate Author
            List<PublisherDto> list = authorClient.getPublishersByAuthor(id);
            sortPublishers(list, sortBy, dir);

            model.addAttribute("targetId", id);
            model.addAttribute("publishers", paginateList(list, page, PAGE_SIZE, model));
            model.addAttribute("endpoint", "/web/authors/relational/publishers/result");
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("dir", dir);
            return "authors/author-publishers";
        } catch (HttpStatusCodeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", extractErrorMessage(e));
            return "redirect:/web/authors/relational/publishers";
        }
    }

    @GetMapping("/relational/stores")
    public String relStores(Model model) {
        model.addAttribute("formTitle", "Get Stores by Author");
        model.addAttribute("actionUrl", "/web/authors/relational/stores/result");
        return "authors/author-id-request";
    }

    @GetMapping("/relational/stores/result")
    public String relStoresResult(
            @RequestParam("id") String id, 
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "sortBy", defaultValue = "storId") String sortBy,
            @RequestParam(value = "dir", defaultValue = "asc") String dir,
            Model model, RedirectAttributes redirectAttributes) {
        try {
            authorClient.getAuthorById(id); // Validate Author
            List<StoreDto> list = authorClient.getStoresByAuthor(id);
            sortStores(list, sortBy, dir);

            model.addAttribute("targetId", id);
            model.addAttribute("stores", paginateList(list, page, PAGE_SIZE, model));
            model.addAttribute("endpoint", "/web/authors/relational/stores/result");
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("dir", dir);
            return "authors/author-stores";
        } catch (HttpStatusCodeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", extractErrorMessage(e));
            return "redirect:/web/authors/relational/stores";
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

    private void sortAuthors(List<AuthorDto> list, String sortBy, String dir) {
        if ("auFname".equals(sortBy)) list.sort(Comparator.comparing(AuthorDto::getAuFname, Comparator.nullsLast(String::compareToIgnoreCase)));
        else if ("auLname".equals(sortBy)) list.sort(Comparator.comparing(AuthorDto::getAuLname, Comparator.nullsLast(String::compareToIgnoreCase)));
        else if ("city".equals(sortBy)) list.sort(Comparator.comparing(AuthorDto::getCity, Comparator.nullsLast(String::compareToIgnoreCase)));
        else if ("state".equals(sortBy)) list.sort(Comparator.comparing(AuthorDto::getState, Comparator.nullsLast(String::compareToIgnoreCase)));
        else if ("contract".equals(sortBy)) list.sort(Comparator.comparing(AuthorDto::getContract, Comparator.nullsLast(Comparator.naturalOrder())));
        else list.sort(Comparator.comparing(AuthorDto::getAuId));
        if ("desc".equals(dir)) Collections.reverse(list);
    }

    private void sortTitleAuthors(List<TitleAuthorDto> list, String sortBy, String dir) {
        if ("auOrd".equals(sortBy)) list.sort(Comparator.comparing(TitleAuthorDto::getAuOrd, Comparator.nullsLast(Comparator.naturalOrder())));
        else if ("royaltyPer".equals(sortBy)) list.sort(Comparator.comparing(TitleAuthorDto::getRoyaltyPer, Comparator.nullsLast(Comparator.naturalOrder())));
        else list.sort(Comparator.comparing(ta -> ta.getTitle() != null ? ta.getTitle().getTitleId() : "", Comparator.nullsLast(String::compareToIgnoreCase)));
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