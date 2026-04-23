package edu.gatech.cs6310.powergrid.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.gatech.cs6310.powergrid.api.Dtos.LedgerEntryView;
import edu.gatech.cs6310.powergrid.service.ReportingService;
import edu.gatech.cs6310.powergrid.service.ReportingService.CompanySummary;
import edu.gatech.cs6310.powergrid.service.ReportingService.SourceBreakdown;

@RestController
@RequestMapping("/api/reports")
public class ReportingController {

    private final ReportingService service;

    public ReportingController(ReportingService service) {
        this.service = service;
    }

    @GetMapping("/companies/{shortName}/summary")
    public CompanySummary summary(
        @PathVariable String shortName,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return service.summarize(shortName, from, to);
    }

    @GetMapping("/companies/{shortName}/ledger")
    public List<LedgerEntryView> ledger(@PathVariable String shortName) {
        return service.ledgerFor(shortName).stream().map(LedgerEntryView::from).toList();
    }

    @GetMapping("/companies/{shortName}/sources")
    public SourceBreakdown sources(
        @PathVariable String shortName,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return service.sourceBreakdown(shortName, from, to);
    }
}
