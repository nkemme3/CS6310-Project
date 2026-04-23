package edu.gatech.cs6310.powergrid.service;

import java.util.Collection;

import org.springframework.stereotype.Service;

import edu.gatech.cs6310.powergrid.domain.Customer;
import edu.gatech.cs6310.powergrid.domain.CustomerType;
import edu.gatech.cs6310.powergrid.domain.Location;
import edu.gatech.cs6310.powergrid.domain.PowerGridSystem;
import edu.gatech.cs6310.powergrid.domain.Transformer;
import edu.gatech.cs6310.powergrid.error.SystemError;
import edu.gatech.cs6310.powergrid.robustness.JournalCommand;
import edu.gatech.cs6310.powergrid.robustness.ProofService;
import edu.gatech.cs6310.powergrid.robustness.TransactionJournal;

@Service
public class CustomerService {

    private final PowerGridSystem pgs;
    private final TransactionJournal journal;

    public CustomerService(PowerGridSystem pgs, TransactionJournal journal) {
        this.pgs = pgs;
        this.journal = journal;
    }

    public Customer createCustomer(String companyShortName, String name, CustomerType type, Location location) {
        ProofService.validateNotBlank("name", name);
        if (type == null) {
            throw SystemError.invalidCommand("customerType is required.", "customerType");
        }
        if (location == null) {
            throw SystemError.invalidCommand("location is required.", "location");
        }
        synchronized (pgs.lock()) {
            ProofService.validateExists("Power company", companyShortName, pgs.companies());
            long accountNumber = pgs.nextAccountNumber();
            JournalCommand.CreateCustomerCmd cmd = new JournalCommand.CreateCustomerCmd(
                accountNumber, companyShortName, name, type, location);
            journal.append(cmd);
            cmd.apply(pgs);
            return pgs.customers().get(accountNumber);
        }
    }

    public void connectCustomerToTransformer(long accountNumber, String transformerId) {
        synchronized (pgs.lock()) {
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
            JournalCommand.ConnectCustomerTransformerCmd cmd = new JournalCommand.ConnectCustomerTransformerCmd(accountNumber, transformerId);
            journal.append(cmd);
            cmd.apply(pgs);
        }
    }

    public Collection<Customer> list() { return pgs.customers().values(); }

    public Customer get(long accountNumber) {
        return ProofService.validateExists("Customer", accountNumber, pgs.customers());
    }
}
