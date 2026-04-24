package edu.gatech.cs6310.powergrid.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.gatech.cs6310.powergrid.api.Dtos.CreateRatePlanRequest;
import edu.gatech.cs6310.powergrid.api.Dtos.RatePlanView;
import edu.gatech.cs6310.powergrid.service.RatePlanService;

@RestController
@RequestMapping("/api/rate-plans")
public class RatePlanController {

    private final RatePlanService service;

    public RatePlanController(RatePlanService service) {
        this.service = service;
    }

    @GetMapping
    public List<RatePlanView> list() {
        return service.list().stream().map(RatePlanView::from).toList();
    }

    @GetMapping("/{id}")
    public RatePlanView get(@PathVariable String id) {
        return RatePlanView.from(service.get(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','BILLING')")
    public ResponseEntity<RatePlanView> create(@RequestBody CreateRatePlanRequest req) {
        RatePlanView v = RatePlanView.from(service.addRatePlan(
            req.planId(), req.companyShortName(), req.ratePerKWh(),
            req.customerType(), req.accountNumber(),
            req.effectiveStart(), req.effectiveEnd()
        ));
        return ResponseEntity.status(201).body(v);
    }
}
