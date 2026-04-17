package com.capg.portal.frontend.controller;

import com.capg.portal.frontend.client.RoyaltyScheduleClient;
import com.capg.portal.frontend.dto.RoyaltyScheduleDto;
import com.capg.portal.frontend.dto.TitleDto;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/web/roysched")
public class RoyaltyScheduleMvcController {

    private final RoyaltyScheduleClient royaltyScheduleClient;

    public RoyaltyScheduleMvcController(RoyaltyScheduleClient royaltyScheduleClient) {
        this.royaltyScheduleClient = royaltyScheduleClient;
    }

    @GetMapping("/operations")
    public String showOperationsMenu() {
        return "roysched/roysched-operations";
    }

    // --- CRUD ---
    @GetMapping("/get-all")
    public String getAll(Model model) {
        model.addAttribute("schedules", royaltyScheduleClient.getAllRoyaltySchedules());
        model.addAttribute("pageTitle", "All Royalty Schedules");
        return "roysched/roysched-list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        RoyaltyScheduleDto dto = new RoyaltyScheduleDto();
        dto.setTitle(new TitleDto()); // Prevents Thymeleaf null reference error for nested titleId
        
        model.addAttribute("schedule", dto);
        model.addAttribute("formTitle", "Create New Royalty Schedule");
        model.addAttribute("actionUrl", "/web/roysched/create/save");
        model.addAttribute("isUpdate", false);
        return "roysched/roysched-form";
    }

    @PostMapping("/create/save")
    public String saveCreate(@Valid @ModelAttribute("schedule") RoyaltyScheduleDto schedule, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("formTitle", "Create New Royalty Schedule");
            model.addAttribute("actionUrl", "/web/roysched/create/save");
            model.addAttribute("isUpdate", false);
            return "roysched/roysched-form";
        }
        try {
            royaltyScheduleClient.createRoyaltySchedule(schedule);
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error: Ensure Low Range is strictly less than High Range.");
            model.addAttribute("formTitle", "Create New Royalty Schedule");
            model.addAttribute("actionUrl", "/web/roysched/create/save");
            model.addAttribute("isUpdate", false);
            return "roysched/roysched-form";
        }
        return "redirect:/web/roysched/get-all";
    }

    @GetMapping("/get-by-id")
    public String showIdRequest(Model model) {
        model.addAttribute("formTitle", "Fetch Schedule by ID");
        model.addAttribute("actionUrl", "/web/roysched/get-by-id/result");
        return "roysched/roysched-id-request";
    }

    @GetMapping("/get-by-id/result")
    public String getIdResult(@RequestParam("id") Integer id, Model model) {
        model.addAttribute("schedule", royaltyScheduleClient.getRoyaltyScheduleById(id));
        return "roysched/roysched-details";
    }

    @GetMapping("/update")
    public String showUpdateIdRequest(Model model) {
        model.addAttribute("formTitle", "Update Schedule (PUT)");
        model.addAttribute("actionUrl", "/web/roysched/update/form");
        return "roysched/roysched-id-request";
    }

    @GetMapping("/update/form")
    public String showUpdateForm(@RequestParam("id") Integer id, Model model) {
        RoyaltyScheduleDto schedule = royaltyScheduleClient.getRoyaltyScheduleById(id);
        if (schedule.getTitle() == null) schedule.setTitle(new TitleDto());

        model.addAttribute("schedule", schedule);
        model.addAttribute("formTitle", "Update Schedule Data");
        model.addAttribute("actionUrl", "/web/roysched/update/save");
        model.addAttribute("isUpdate", true);
        return "roysched/roysched-form";
    }

    @PostMapping("/update/save")
    public String saveUpdate(@Valid @ModelAttribute("schedule") RoyaltyScheduleDto schedule, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("formTitle", "Update Schedule Data");
            model.addAttribute("actionUrl", "/web/roysched/update/save");
            model.addAttribute("isUpdate", true);
            return "roysched/roysched-form";
        }
        try {
            royaltyScheduleClient.updateRoyaltySchedule(schedule.getRoyschedId(), schedule);
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error: Ensure Low Range is strictly less than High Range.");
            model.addAttribute("formTitle", "Update Schedule Data");
            model.addAttribute("actionUrl", "/web/roysched/update/save");
            model.addAttribute("isUpdate", true);
            return "roysched/roysched-form";
        }
        return "redirect:/web/roysched/get-all";
    }

    @GetMapping("/patch")
    public String showPatchIdRequest(Model model) {
        model.addAttribute("formTitle", "Patch Schedule");
        model.addAttribute("actionUrl", "/web/roysched/patch/form");
        return "roysched/roysched-id-request";
    }

    @GetMapping("/patch/form")
    public String showPatchForm(@RequestParam("id") Integer id, Model model) {
        RoyaltyScheduleDto schedule = royaltyScheduleClient.getRoyaltyScheduleById(id);
        if (schedule.getTitle() == null) schedule.setTitle(new TitleDto());

        model.addAttribute("schedule", schedule);
        model.addAttribute("formTitle", "Patch Schedule Data");
        model.addAttribute("actionUrl", "/web/roysched/patch/save");
        model.addAttribute("isUpdate", true);
        return "roysched/roysched-form";
    }

    @PostMapping("/patch/save")
    public String savePatch(@ModelAttribute("schedule") RoyaltyScheduleDto schedule, Model model) {
        try {
            royaltyScheduleClient.patchRoyaltySchedule(schedule.getRoyschedId(), schedule);
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error: Check Range logic constraint.");
            model.addAttribute("formTitle", "Patch Schedule Data");
            model.addAttribute("actionUrl", "/web/roysched/patch/save");
            model.addAttribute("isUpdate", true);
            return "roysched/roysched-form";
        }
        return "redirect:/web/roysched/get-all";
    }

    // --- FILTERS ---
    @GetMapping("/filter/range")
    public String filterRange() {
        return "roysched/roysched-range-request";
    }

    @GetMapping("/filter/range/result")
    public String filterRangeResult(@RequestParam("minLorange") Integer minLorange, @RequestParam("maxHirange") Integer maxHirange, Model model) {
        model.addAttribute("schedules", royaltyScheduleClient.filterRoyaltyByRange(minLorange, maxHirange));
        model.addAttribute("pageTitle", "Schedules for Range " + minLorange + " to " + maxHirange);
        return "roysched/roysched-list";
    }

    @GetMapping("/filter/title")
    public String filterTitle() {
        return "roysched/roysched-title-request";
    }

    @GetMapping("/filter/title/result")
    public String filterTitleResult(@RequestParam("titleId") String titleId, Model model) {
        model.addAttribute("schedules", royaltyScheduleClient.filterRoyaltyByTitle(titleId));
        model.addAttribute("pageTitle", "Royalty Schedules for Title ID: " + titleId);
        return "roysched/roysched-list";
    }

    // --- RELATIONAL (1-Hop) ---
    @GetMapping("/title")
    public String relTitleRequest(Model model) {
        model.addAttribute("formTitle", "Get Title by Schedule ID");
        model.addAttribute("actionUrl", "/web/roysched/title/result");
        return "roysched/roysched-id-request"; // Using standard Integer ID form
    }

    @GetMapping("/title/result")
    public String relTitleResult(@RequestParam("id") Integer id, Model model) {
        model.addAttribute("targetId", id);
        model.addAttribute("title", royaltyScheduleClient.getTitleByRoyaltySchedule(id));
        return "roysched/roysched-title-result";
    }
}