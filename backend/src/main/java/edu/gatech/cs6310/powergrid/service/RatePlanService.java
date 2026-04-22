package edu.gatech.cs6310.powergrid.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;

import org.springframework.stereotype.Service;

import edu.gatech.cs6310.powergrid.domain.Customer;
import edu.gatech.cs6310.powergrid.domain.CustomerType;
import edu.gatech.cs6310.powergrid.domain.PowerCompany;
import edu.gatech.cs6310.powergrid.domain.PowerGridSystem;
import edu.gatech.cs6310.powergrid.domain.RatePlan;
import edu.gatech.cs6310.powergrid.error.SystemError;
import edu.gatech.cs6310.powergrid.robustness.ProofService;

@Service
public class RatePlanService {

    private final PowerGridSystem pgs;

    public RatePlanService(PowerGridSystem pgs) {
        this.pgs = pgs;
    }

    public RatePlan addRatePlan(String planId, String companyShortName, BigDecimal ratePerKWh,
                                CustomerType customerType, Long accountNumber,
                                LocalDate effectiveStart, LocalDate effectiveEnd) {
        ProofService.validateNotBlank("planId", planId);
        ProofService.validatePositive("ratePerKWh", ratePerKWh);
        if (customerType == null) {
            throw SystemError.invalidCommand("customerType is required.", "customerType");
        }
        if (effectiveStart == null || effectiveEnd == null) {
            throw SystemError.invalidCommand("effectiveStart and effectiveEnd are required.", "effectiveStart");
        }
        if (effectiveEnd.isBefore(effectiveStart)) {
            throw SystemError.invalidCommand("effectiveEnd must be on or after effectiveStart.", "effectiveEnd");
        }
        PowerCompany company = ProofService.validateExists("Power company", companyShortName, pgs.companies());
        ProofService.validateAvailableId("Rate plan", planId, pgs.ratePlans());
        if (accountNumber != null) {
            Customer c = ProofService.validateExists("Customer", accountNumber, pgs.customers());
            if (!c.getCompanyShortName().equals(companyShortName)) {
                throw SystemError.invalidState("Rate plan customer must belong to company '" + companyShortName + "'.");
            }
            if (c.getCustomerType() != customerType) {
                throw SystemError.invalidState("Rate plan customerType must match the customer's type.");
            }
        }
        RatePlan plan = new RatePlan(planId, companyShortName, ratePerKWh, customerType, accountNumber,
            effectiveStart, effectiveEnd);
        pgs.ratePlans().put(planId, plan);
        company.getRatePlanIds().add(planId);
        return plan;
    }

    /**
     * Account-specific plans outrank generic plans for the same (company, type, date).
     */
    public BigDecimal resolveRate(Customer customer, LocalDate onDate) {
        Optional<RatePlan> best = pgs.ratePlans().values().stream()
            .filter(p -> p.getCompanyShortName().equals(customer.getCompanyShortName()))
            .filter(p -> p.appliesTo(customer, onDate))
            .max(Comparator.comparing(RatePlan::isAccountSpecific));
        if (best.isPresent()) {
            return best.get().getRatePerKWh();
        }
        PowerCompany company = pgs.companies().get(customer.getCompanyShortName());
        return company.getStandardRate();
    }

    public Collection<RatePlan> list() { return pgs.ratePlans().values(); }

    public RatePlan get(String id) {
        return ProofService.validateExists("Rate plan", id, pgs.ratePlans());
    }
}
