package com.capg.portal.frontend.client;

import com.capg.portal.frontend.dto.TitleAuthorDto;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.*;

import java.util.List;

public interface TitleAuthorClient {

    @GetExchange("/titleauthors")
    List<TitleAuthorDto> getAllTitleAuthors();

    @GetExchange("/titleauthors/{auId}/{titleId}")
    TitleAuthorDto getTitleAuthorById(@PathVariable("auId") String auId, @PathVariable("titleId") String titleId);

    @PostExchange("/titleauthors")
    TitleAuthorDto createTitleAuthor(@RequestBody TitleAuthorDto titleAuthor);

    @PutExchange("/titleauthors/{auId}/{titleId}")
    TitleAuthorDto updateTitleAuthor(@PathVariable("auId") String auId, @PathVariable("titleId") String titleId, @RequestBody TitleAuthorDto titleAuthor);

    @PatchExchange("/titleauthors/{auId}/{titleId}")
    TitleAuthorDto patchTitleAuthor(@PathVariable("auId") String auId, @PathVariable("titleId") String titleId, @RequestBody TitleAuthorDto updates);

    @GetExchange("/titleauthors/filter/lead")
    List<TitleAuthorDto> getLeadAuthors();

    @GetExchange("/titleauthors/filter/author")
    List<TitleAuthorDto> filterTitleAuthorsByAuthorId(@RequestParam("auId") String auId);

    @GetExchange("/titleauthors/filter/title")
    List<TitleAuthorDto> filterTitleAuthorsByTitleId(@RequestParam("titleId") String titleId);

    @GetExchange("/titleauthors/filter/royalty")
    List<TitleAuthorDto> filterTitleAuthorsByRoyalty(@RequestParam("maxRoyalty") Integer maxRoyalty);

    @GetExchange("/titleauthors/search")
    List<TitleAuthorDto> search(
            @RequestParam(value = "auId", required = false) String auId,
            @RequestParam(value = "titleId", required = false) String titleId,
            @RequestParam(value = "maxRoyalty", required = false) Integer maxRoyalty,
            @RequestParam(value = "minRoyalty", required = false) Integer minRoyalty
    );
}