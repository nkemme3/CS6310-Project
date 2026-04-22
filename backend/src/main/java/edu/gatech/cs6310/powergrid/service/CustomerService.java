package edu.gatech.cs6310.powergrid.service;

import java.util.Collection;

import org.springframework.stereotype.Service;

import edu.gatech.cs6310.powergrid.domain.Customer;
import edu.gatech.cs6310.powergrid.domain.CustomerType;
import edu.gatech.cs6310.powergrid.domain.Location;
import edu.gatech.cs6310.powergrid.domain.PowerCompany;
import edu.gatech.cs6310.powergrid.domain.PowerGridSystem;
import edu.gatech.cs6310.powergrid.domain.Transformer;
import edu.gatech.cs6310.powergrid.error.SystemError;
import edu.gatech.cs6310.powergrid.robustness.ProofService;

@Service
public class CustomerService {

    private final PowerGridSystem pgs;

    public CustomerService(PowerGridSystem pgs) {
        this.pgs = pgs;
    }

    public Customer createCustomer(String companyShortName, String name, CustomerType type, Location location) {
        ProofService.validateNotBlank("name", name);
        if (type == null) {
            throw SystemError.invalidCommand("customerType is required.", "customerType");
        }
        if (location == null) {
            throw SystemError.invalidCommand("location is required.", "location");
        }
        PowerCompany company = ProofService.validateExists("Power company", companyShortName, pgs.companies());
        long accountNumber = pgs.nextAccountNumber();
        Customer c = new Customer(accountNumber, companyShortName, name, type, location);
        pgs.customers().put(accountNumber, c);
        company.getCustomerAccountNumbers().add(accountNumber);
        return c;
    }

    public void connectCustomerToTransformer(long accountNumber, String transformerId) {
        Customer c = ProofService.validateExists("Customer", accountNumber, pgs.customers());
        Transformer t = ProofService.validateExists("Transformer", transformerId, pgs.transformers());
        if (!c.getCompanyShortName().equals(t.getCompanyShortName())) {
            throw SystemError.invalidState("Customer and transformer must belong to the same company.");
        }
        if (c.getConnectedTransformerId().isPresent()) {
            throw new SystemError(
                edu.gatech.cs6310.powergrid.error.ErrorCode.ALREADY_CONNECTED,
                "Customer " + accountNumber + " is already connected to transformer '" + c.getConnectedTransformerId().get() + "'.",
                null,
                "Disconnect the customer before reconnecting."
            );
        }
        ProofService.validateCapacity("Transformer '" + transformerId + "'", t.customerCount(), t.getMaxCustomers());
        ProofService.validateDistance(t.getLocation(), c.getLocation(),
            pgs.getMaxTransformerCustomerDistance(), "transformer-to-customer");
        t.attachCustomer(accountNumber);
        c.setConnectedTransformerId(transformerId);
    }

    public Collection<Customer> list() { return pgs.customers().values(); }

    public Customer get(long accountNumber) {
        return ProofService.validateExists("Customer", accountNumber, pgs.customers());
    }
}
