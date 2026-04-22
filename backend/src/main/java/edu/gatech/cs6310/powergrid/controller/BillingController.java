package edu.gatech.cs6310.powergrid.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.gatech.cs6310.powergrid.api.Dtos.BillingRunResponse;
import edu.gatech.cs6310.powergrid.api.Dtos.LedgerEntryView;
import edu.gatech.cs6310.powergrid.api.Dtos.RecordProductionRequest;
import edu.gatech.cs6310.powergrid.api.Dtos.RecordUsageRequest;
import edu.gatech.cs6310.powergrid.api.Dtos.RunBillingRequest;
import edu.gatech.cs6310.powergrid.service.BillingService;
import edu.gatech.cs6310.powergrid.service.UsageService;

@RestController
@RequestMapping("/api/billing")
public class BillingController {

    private final BillingService billing;
    private final UsageService usage;

    public BillingController(BillingService billing, UsageService usage) {
        this.billing = billing;
        this.usage = usage;
    }

    @PostMapping("/usage")
    public ResponseEntity<LedgerEntryView> recordUsage(@RequestBody RecordUsageRequest req) {
        LedgerEntryView v = LedgerEntryView.from(usage.recordUsage(
            req.accountNumber(), req.periodStart(), req.periodEnd(), req.kWh()));
        return ResponseEntity.status(201).body(v);
    }

    @PostMapping("/production")
    public ResponseEntity<LedgerEntryView> recordProduction(@RequestBody RecordProductionRequest req) {
        LedgerEntryView v = LedgerEntryView.from(usage.recordProduction(
            req.plantId(), req.periodStart(), req.periodEnd(), req.kWh()));
        return ResponseEntity.status(201).body(v);
    }

    @PostMapping("/run")
    public BillingRunResponse run(@RequestBody RunBillingRequest req) {
        return BillingRunResponse.of(billing.runBillingCycle(
            req.companyShortName(), req.periodStart(), req.periodEnd()));
    }
}
