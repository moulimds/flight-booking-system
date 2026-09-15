package com.flight.search.controller;

import com.flight.search.dto.FlightSearchResult;
import com.flight.search.service.SearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private static final Logger logger = LoggerFactory.getLogger(SearchController.class);

    @Autowired
    private SearchService searchService;

    @GetMapping("/flights")
    public ResponseEntity<List<FlightSearchResult>> searchFlights(
            @RequestParam(value = "source", required = false) String source,
            @RequestParam(value = "destination", required = false) String destination,
            @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        logger.info("REST request to search flights: source={}, destination={}, date={}", source, destination, date);
        List<FlightSearchResult> results = searchService.searchFlights(source, destination, date);
        return ResponseEntity.ok(results);
    }

    @PostMapping("/sync")
    public ResponseEntity<String> syncFlightIndex() {
        logger.info("REST request to sync search index");
        searchService.syncFlightData();
        return ResponseEntity.ok("Search index synchronization completed");
    }
}
