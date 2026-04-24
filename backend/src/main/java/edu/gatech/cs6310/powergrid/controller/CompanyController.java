package edu.gatech.cs6310.powergrid.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.gatech.cs6310.powergrid.api.Dtos.CompanyView;
import edu.gatech.cs6310.powergrid.api.Dtos.CreateCompanyRequest;
import edu.gatech.cs6310.powergrid.api.Dtos.UpdateStandardRateRequest;
import edu.gatech.cs6310.powergrid.service.CompanyService;

@RestController
@RequestMapping("/api/companies")
public class CompanyController {

    private final CompanyService service;

    public CompanyController(CompanyService service) {
        this.service = service;
    }

    @GetMapping
    public List<CompanyView> list() {
        return service.list().stream().map(CompanyView::from).toList();
    }

    @GetMapping("/{shortName}")
    public CompanyView get(@PathVariable String shortName) {
        return CompanyView.from(service.get(shortName));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    public ResponseEntity<CompanyView> create(@RequestBody CreateCompanyRequest req) {
        CompanyView v = CompanyView.from(service.addCompany(req.longName(), req.shortName(), req.standardRate()));
        return ResponseEntity.status(201).body(v);
    }

    @PutMapping("/{shortName}/standard-rate")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','BILLING')")
    public CompanyView updateRate(@PathVariable String shortName, @RequestBody UpdateStandardRateRequest req) {
        return CompanyView.from(service.updateStandardRate(shortName, req.standardRate()));
    }
}
