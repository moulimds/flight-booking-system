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

    @GetMapping({"", "/flights"})
    public ResponseEntity<List<FlightSearchResult>> searchFlights(
            @RequestParam(value = "source", required = false) String source,
            @RequestParam(value = "origin", required = false) String origin,
            @RequestParam(value = "destination", required = false) String destination,
            @RequestParam(value = "dest", required = false) String dest,
            @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(value = "departureDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate departureDate) {
        String finalSource = (source != null && !source.trim().isEmpty()) ? source : origin;
        String finalDestination = (destination != null && !destination.trim().isEmpty()) ? destination : dest;
        LocalDate finalDate = (date != null) ? date : departureDate;

        logger.info("REST request to search flights: source={}, destination={}, date={}", finalSource, finalDestination, finalDate);
        List<FlightSearchResult> results = searchService.searchFlights(finalSource, finalDestination, finalDate);
        return ResponseEntity.ok(results);
    }

    @PostMapping("/sync")
    public ResponseEntity<String> syncFlightIndex() {
        logger.info("REST request to sync search index");
        searchService.syncFlightData();
        return ResponseEntity.ok("Search index synchronization completed");
    }
}
