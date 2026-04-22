package edu.gatech.cs6310.powergrid.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.gatech.cs6310.powergrid.api.Dtos.ConnectCustomerRequest;
import edu.gatech.cs6310.powergrid.api.Dtos.CreateCustomerRequest;
import edu.gatech.cs6310.powergrid.api.Dtos.CustomerView;
import edu.gatech.cs6310.powergrid.service.CustomerService;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService service;

    public CustomerController(CustomerService service) {
        this.service = service;
    }

    @GetMapping
    public List<CustomerView> list() {
        return service.list().stream().map(CustomerView::from).toList();
    }

    @GetMapping("/{accountNumber}")
    public CustomerView get(@PathVariable long accountNumber) {
        return CustomerView.from(service.get(accountNumber));
    }

    @PostMapping
    public ResponseEntity<CustomerView> create(@RequestBody CreateCustomerRequest req) {
        CustomerView v = CustomerView.from(service.createCustomer(
            req.companyShortName(), req.name(), req.customerType(),
            req.location() == null ? null : req.location().toDomain()
        ));
        return ResponseEntity.status(201).body(v);
    }

    @PostMapping("/connect")
    public ResponseEntity<Void> connect(@RequestBody ConnectCustomerRequest req) {
        service.connectCustomerToTransformer(req.accountNumber(), req.transformerId());
        return ResponseEntity.noContent().build();
    }
}
