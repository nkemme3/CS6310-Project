package edu.gatech.cs6310.powergrid.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.gatech.cs6310.powergrid.api.Dtos.CreateEmployeeRequest;
import edu.gatech.cs6310.powergrid.api.Dtos.EmployeeView;
import edu.gatech.cs6310.powergrid.service.EmployeeService;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService service;

    public EmployeeController(EmployeeService service) {
        this.service = service;
    }

    @GetMapping
    public List<EmployeeView> list() {
        return service.list().stream().map(EmployeeView::from).toList();
    }

    @GetMapping("/{id}")
    public EmployeeView get(@PathVariable String id) {
        return EmployeeView.from(service.get(id));
    }

    @PostMapping
    public ResponseEntity<EmployeeView> create(@RequestBody CreateEmployeeRequest req) {
        EmployeeView v = EmployeeView.from(service.addEmployee(
            req.companyShortName(), req.employeeId(), req.name(), req.startDate(), req.hourlyWage()
        ));
        return ResponseEntity.status(201).body(v);
    }
}
