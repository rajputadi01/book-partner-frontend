package com.capg.portal.frontend.controller;

import com.capg.portal.frontend.client.RoyaltyScheduleClient;
import com.capg.portal.frontend.dto.RoyaltyScheduleDto;
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
@RequestMapping("/web/roysched")
public class RoyaltyScheduleMvcController {

    private final RoyaltyScheduleClient royaltyScheduleClient;
    private final int PAGE_SIZE = 5;

    public RoyaltyScheduleMvcController(RoyaltyScheduleClient royaltyScheduleClient) {
        this.royaltyScheduleClient = royaltyScheduleClient;
    }

    @GetMapping("/operations")
    public String showOperationsMenu() {
        return "roysched/roysched-operations";
    }

    // --- CRUD ---
    @GetMapping("/get-all")
    public String getAll(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "sortBy", defaultValue = "royschedId") String sortBy,
            @RequestParam(value = "dir", defaultValue = "asc") String dir,
            Model model) {
        
        List<RoyaltyScheduleDto> list = royaltyScheduleClient.getAllRoyaltySchedules();
        sortSchedules(list, sortBy, dir);

        model.addAttribute("schedules", paginateList(list, page, PAGE_SIZE, model));
        model.addAttribute("pageTitle", "All Royalty Schedules");
        model.addAttribute("endpoint", "/web/roysched/get-all");
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("dir", dir);
        return "roysched/roysched-list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        RoyaltyScheduleDto dto = new RoyaltyScheduleDto();
        dto.setTitle(new TitleDto()); 
        
        model.addAttribute("schedule", dto);
        model.addAttribute("formTitle", "Create New Royalty Schedule");
        model.addAttribute("actionUrl", "/web/roysched/create/save");
        model.addAttribute("isUpdate", false);
        return "roysched/roysched-form";
    }

    @PostMapping("/create/save")
    public String saveCreate(@Valid @ModelAttribute("schedule") RoyaltyScheduleDto schedule, BindingResult result, Model model) {
        // Pre-Flight Validation
        if (schedule.getLorange() != null && schedule.getHirange() != null && schedule.getLorange() >= schedule.getHirange()) {
            result.rejectValue("lorange", "error.schedule", "Low Range must be strictly less than High Range.");
        }
        
        if (result.hasErrors()) {
            model.addAttribute("formTitle", "Create New Royalty Schedule");
            model.addAttribute("actionUrl", "/web/roysched/create/save");
            model.addAttribute("isUpdate", false);
            return "roysched/roysched-form";
        }
        
        try {
            royaltyScheduleClient.createRoyaltySchedule(schedule);
            return "redirect:/web/roysched/get-all";
        } catch (HttpStatusCodeException e) {
            model.addAttribute("errorMessage", extractErrorMessage(e));
            model.addAttribute("formTitle", "Create New Royalty Schedule");
            model.addAttribute("actionUrl", "/web/roysched/create/save");
            model.addAttribute("isUpdate", false);
            return "roysched/roysched-form";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "An unexpected error occurred: " + e.getMessage());
            model.addAttribute("formTitle", "Create New Royalty Schedule");
            model.addAttribute("actionUrl", "/web/roysched/create/save");
            model.addAttribute("isUpdate", false);
            return "roysched/roysched-form";
        }
    }

    @GetMapping("/get-by-id")
    public String showIdRequest(Model model) {
        model.addAttribute("formTitle", "Fetch Schedule by ID");
        model.addAttribute("actionUrl", "/web/roysched/get-by-id/result");
        return "roysched/roysched-id-request";
    }

    @GetMapping("/get-by-id/result")
    public String getIdResult(@RequestParam("id") Integer id, Model model, RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("schedule", royaltyScheduleClient.getRoyaltyScheduleById(id));
            return "roysched/roysched-details";
        } catch (HttpStatusCodeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", extractErrorMessage(e));
            return "redirect:/web/roysched/get-by-id";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred: " + e.getMessage());
            return "redirect:/web/roysched/get-by-id";
        }
    }

    @GetMapping("/update")
    public String showUpdateIdRequest(Model model) {
        model.addAttribute("formTitle", "Update Schedule (PUT)");
        model.addAttribute("actionUrl", "/web/roysched/update/form");
        return "roysched/roysched-id-request";
    }

    @GetMapping("/update/form")
    public String showUpdateForm(@RequestParam("id") Integer id, Model model, RedirectAttributes redirectAttributes) {
        try {
            RoyaltyScheduleDto schedule = royaltyScheduleClient.getRoyaltyScheduleById(id);
            if (schedule.getTitle() == null) schedule.setTitle(new TitleDto());

            model.addAttribute("schedule", schedule);
            model.addAttribute("formTitle", "Update Schedule Data");
            model.addAttribute("actionUrl", "/web/roysched/update/save");
            model.addAttribute("isUpdate", true);
            return "roysched/roysched-form";
        } catch (HttpStatusCodeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", extractErrorMessage(e));
            return "redirect:/web/roysched/update";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred: " + e.getMessage());
            return "redirect:/web/roysched/update";
        }
    }

    @PostMapping("/update/save")
    public String saveUpdate(@Valid @ModelAttribute("schedule") RoyaltyScheduleDto schedule, BindingResult result, Model model) {
        if (schedule.getLorange() != null && schedule.getHirange() != null && schedule.getLorange() >= schedule.getHirange()) {
            result.rejectValue("lorange", "error.schedule", "Low Range must be strictly less than High Range.");
        }
        
        if (result.hasErrors()) {
            model.addAttribute("formTitle", "Update Schedule Data");
            model.addAttribute("actionUrl", "/web/roysched/update/save");
            model.addAttribute("isUpdate", true);
            return "roysched/roysched-form";
        }
        
        try {
            royaltyScheduleClient.updateRoyaltySchedule(schedule.getRoyschedId(), schedule);
            return "redirect:/web/roysched/get-all";
        } catch (HttpStatusCodeException e) {
            model.addAttribute("errorMessage", extractErrorMessage(e));
            model.addAttribute("formTitle", "Update Schedule Data");
            model.addAttribute("actionUrl", "/web/roysched/update/save");
            model.addAttribute("isUpdate", true);
            return "roysched/roysched-form";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "An unexpected error occurred: " + e.getMessage());
            model.addAttribute("formTitle", "Update Schedule Data");
            model.addAttribute("actionUrl", "/web/roysched/update/save");
            model.addAttribute("isUpdate", true);
            return "roysched/roysched-form";
        }
    }

    @GetMapping("/patch")
    public String showPatchIdRequest(Model model) {
        model.addAttribute("formTitle", "Patch Schedule");
        model.addAttribute("actionUrl", "/web/roysched/patch/form");
        return "roysched/roysched-id-request";
    }

    @GetMapping("/patch/form")
    public String showPatchForm(@RequestParam("id") Integer id, Model model, RedirectAttributes redirectAttributes) {
        try {
            // Read-Only Reference Data
            model.addAttribute("currentRecord", royaltyScheduleClient.getRoyaltyScheduleById(id));
            
            // Blank Patch DTO
            RoyaltyScheduleDto patchDto = new RoyaltyScheduleDto();
            patchDto.setRoyschedId(id);
            patchDto.setTitle(new TitleDto());
            
            model.addAttribute("schedule", patchDto);
            model.addAttribute("formTitle", "Patch Schedule Data");
            model.addAttribute("actionUrl", "/web/roysched/patch/save");
            return "roysched/roysched-patch";
        } catch (HttpStatusCodeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", extractErrorMessage(e));
            return "redirect:/web/roysched/patch";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred: " + e.getMessage());
            return "redirect:/web/roysched/patch";
        }
    }

    @PostMapping("/patch/save")
    public String savePatch(@ModelAttribute("schedule") RoyaltyScheduleDto schedule, RedirectAttributes redirectAttributes) {
        // Business Validation for partial updates
        if (schedule.getLorange() != null && schedule.getHirange() != null && schedule.getLorange() >= schedule.getHirange()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Validation Error: Low Range must be strictly less than High Range.");
            return "redirect:/web/roysched/patch/form?id=" + schedule.getRoyschedId();
        }

        try {
            royaltyScheduleClient.patchRoyaltySchedule(schedule.getRoyschedId(), schedule);
            return "redirect:/web/roysched/get-all";
        } catch (HttpStatusCodeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", extractErrorMessage(e));
            return "redirect:/web/roysched/patch/form?id=" + schedule.getRoyschedId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred: " + e.getMessage());
            return "redirect:/web/roysched/patch/form?id=" + schedule.getRoyschedId();
        }
    }

    // --- FILTERS ---
    @GetMapping("/filter/range")
    public String filterRange() {
        return "roysched/roysched-range-request";
    }

    @GetMapping("/filter/range/result")
    public String filterRangeResult(
            @RequestParam("minLorange") Integer minLorange, 
            @RequestParam("maxHirange") Integer maxHirange, 
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "sortBy", defaultValue = "royschedId") String sortBy,
            @RequestParam(value = "dir", defaultValue = "asc") String dir,
            Model model, RedirectAttributes redirectAttributes) {
        
        // Pre-flight sanity check for the GET request
        if (minLorange > maxHirange) {
            redirectAttributes.addFlashAttribute("errorMessage", "Minimum Low Range cannot be greater than Maximum High Range.");
            return "redirect:/web/roysched/filter/range";
        }

        try {
            List<RoyaltyScheduleDto> list = royaltyScheduleClient.filterRoyaltyByRange(minLorange, maxHirange);
            sortSchedules(list, sortBy, dir);

            model.addAttribute("schedules", paginateList(list, page, PAGE_SIZE, model));
            model.addAttribute("pageTitle", "Schedules for Range " + minLorange + " to " + maxHirange);
            model.addAttribute("endpoint", "/web/roysched/filter/range/result");
            model.addAttribute("minLorange", minLorange);
            model.addAttribute("maxHirange", maxHirange);
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("dir", dir);
            return "roysched/roysched-list";
        } catch (HttpStatusCodeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", extractErrorMessage(e));
            return "redirect:/web/roysched/filter/range";
        }
    }

    @GetMapping("/filter/title")
    public String filterTitle() {
        return "roysched/roysched-title-request";
    }

    @GetMapping("/filter/title/result")
    public String filterTitleResult(
            @RequestParam("titleId") String titleId, 
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "sortBy", defaultValue = "royschedId") String sortBy,
            @RequestParam(value = "dir", defaultValue = "asc") String dir,
            Model model, RedirectAttributes redirectAttributes) {
        try {
            List<RoyaltyScheduleDto> list = royaltyScheduleClient.filterRoyaltyByTitle(titleId);
            sortSchedules(list, sortBy, dir);

            model.addAttribute("schedules", paginateList(list, page, PAGE_SIZE, model));
            model.addAttribute("pageTitle", "Royalty Schedules for Title ID: " + titleId);
            model.addAttribute("endpoint", "/web/roysched/filter/title/result");
            model.addAttribute("titleId", titleId);
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("dir", dir);
            return "roysched/roysched-list";
        } catch (HttpStatusCodeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", extractErrorMessage(e));
            return "redirect:/web/roysched/filter/title";
        }
    }

    // --- RELATIONAL (1-Hop) ---
    @GetMapping("/title")
    public String relTitleRequest(Model model) {
        model.addAttribute("formTitle", "Get Title by Schedule ID");
        model.addAttribute("actionUrl", "/web/roysched/title/result");
        return "roysched/roysched-id-request";
    }

    @GetMapping("/title/result")
    public String relTitleResult(@RequestParam("id") Integer id, Model model, RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("targetId", id);
            model.addAttribute("title", royaltyScheduleClient.getTitleByRoyaltySchedule(id));
            return "roysched/roysched-title-result";
        } catch (HttpStatusCodeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", extractErrorMessage(e));
            return "redirect:/web/roysched/title";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred: " + e.getMessage());
            return "redirect:/web/roysched/title";
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

    private void sortSchedules(List<RoyaltyScheduleDto> list, String sortBy, String dir) {
        if ("titleId".equals(sortBy)) list.sort(Comparator.comparing(s -> s.getTitle() != null ? s.getTitle().getTitleId() : "", Comparator.nullsLast(String::compareToIgnoreCase)));
        else if ("lorange".equals(sortBy)) list.sort(Comparator.comparing(RoyaltyScheduleDto::getLorange, Comparator.nullsLast(Comparator.naturalOrder())));
        else if ("hirange".equals(sortBy)) list.sort(Comparator.comparing(RoyaltyScheduleDto::getHirange, Comparator.nullsLast(Comparator.naturalOrder())));
        else if ("royalty".equals(sortBy)) list.sort(Comparator.comparing(RoyaltyScheduleDto::getRoyalty, Comparator.nullsLast(Comparator.naturalOrder())));
        else list.sort(Comparator.comparing(RoyaltyScheduleDto::getRoyschedId, Comparator.nullsLast(Comparator.naturalOrder())));
        
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