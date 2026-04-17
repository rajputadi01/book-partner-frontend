package com.capg.portal.frontend.client;

import com.capg.portal.frontend.dto.RoyaltyScheduleDto;
import com.capg.portal.frontend.dto.TitleDto;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.*;

import java.util.List;

public interface RoyaltyScheduleClient {

    @GetExchange("/roysched")
    List<RoyaltyScheduleDto> getAllRoyaltySchedules();

    @GetExchange("/roysched/{id}")
    RoyaltyScheduleDto getRoyaltyScheduleById(@PathVariable("id") Integer id);

    @PostExchange("/roysched")
    RoyaltyScheduleDto createRoyaltySchedule(@RequestBody RoyaltyScheduleDto royaltySchedule);

    @PutExchange("/roysched/{id}")
    RoyaltyScheduleDto updateRoyaltySchedule(@PathVariable("id") Integer id, @RequestBody RoyaltyScheduleDto royaltySchedule);

    @PatchExchange("/roysched/{id}")
    RoyaltyScheduleDto patchRoyaltySchedule(@PathVariable("id") Integer id, @RequestBody RoyaltyScheduleDto updates);

    @GetExchange("/roysched/filter/range")
    List<RoyaltyScheduleDto> filterRoyaltyByRange(@RequestParam("minLorange") Integer minLorange, @RequestParam("maxHirange") Integer maxHirange);

    @GetExchange("/roysched/filter/title")
    List<RoyaltyScheduleDto> filterRoyaltyByTitle(@RequestParam("titleId") String titleId);

    @GetExchange("/roysched/{id}/title")
    TitleDto getTitleByRoyaltySchedule(@PathVariable("id") Integer id);
}