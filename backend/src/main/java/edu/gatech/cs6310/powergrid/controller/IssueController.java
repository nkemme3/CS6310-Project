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

import edu.gatech.cs6310.powergrid.api.Dtos.AssignIssueRequest;
import edu.gatech.cs6310.powergrid.api.Dtos.IssueView;
import edu.gatech.cs6310.powergrid.api.Dtos.ReportIssueRequest;
import edu.gatech.cs6310.powergrid.service.IssueService;

@RestController
@RequestMapping("/api/issues")
public class IssueController {

    private final IssueService service;

    public IssueController(IssueService service) {
        this.service = service;
    }

    @GetMapping
    public List<IssueView> list() {
        return service.list().stream().map(IssueView::from).toList();
    }

    @GetMapping("/{id}")
    public IssueView get(@PathVariable long id) {
        return IssueView.from(service.get(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','FIELD')")
    public ResponseEntity<IssueView> report(@RequestBody ReportIssueRequest req) {
        IssueView v = IssueView.from(service.reportIssue(
            req.companyShortName(), req.assetType(), req.assetId(),
            req.hoursRequired(), req.materialsCost()
        ));
        return ResponseEntity.status(201).body(v);
    }

    @PostMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    public IssueView assign(@PathVariable long id, @RequestBody AssignIssueRequest req) {
        return IssueView.from(service.assignIssue(id, req.employeeId()));
    }

    @PostMapping("/{id}/resolve")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','FIELD')")
    public IssueView resolve(@PathVariable long id) {
        return IssueView.from(service.resolveIssue(id));
    }
}
