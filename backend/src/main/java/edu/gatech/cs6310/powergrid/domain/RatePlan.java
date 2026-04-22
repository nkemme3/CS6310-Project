package edu.gatech.cs6310.powergrid.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

public class RatePlan {

    private final String planId;
    private final String companyShortName;
    private final BigDecimal ratePerKWh;
    private final CustomerType customerType;
    private final Long accountNumber;
    private final LocalDate effectiveStart;
    private final LocalDate effectiveEnd;

    public RatePlan(String planId, String companyShortName, BigDecimal ratePerKWh,
                    CustomerType customerType, Long accountNumber,
                    LocalDate effectiveStart, LocalDate effectiveEnd) {
        this.planId = planId;
        this.companyShortName = companyShortName;
        this.ratePerKWh = ratePerKWh;
        this.customerType = customerType;
        this.accountNumber = accountNumber;
        this.effectiveStart = effectiveStart;
        this.effectiveEnd = effectiveEnd;
    }

    public String getPlanId() { return planId; }
    public String getCompanyShortName() { return companyShortName; }
    public BigDecimal getRatePerKWh() { return ratePerKWh; }
    public CustomerType getCustomerType() { return customerType; }
    public Optional<Long> getAccountNumber() { return Optional.ofNullable(accountNumber); }
    public LocalDate getEffectiveStart() { return effectiveStart; }
    public LocalDate getEffectiveEnd() { return effectiveEnd; }

    public boolean appliesTo(Customer customer, LocalDate onDate) {
        if (customer.getCustomerType() != customerType) return false;
        if (onDate.isBefore(effectiveStart) || onDate.isAfter(effectiveEnd)) return false;
        if (accountNumber != null && accountNumber != customer.getAccountNumber()) return false;
        return true;
    }

    public boolean isAccountSpecific() { return accountNumber != null; }
}
