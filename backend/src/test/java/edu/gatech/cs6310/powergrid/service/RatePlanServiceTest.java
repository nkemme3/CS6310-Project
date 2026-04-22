package edu.gatech.cs6310.powergrid.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.gatech.cs6310.powergrid.domain.Customer;
import edu.gatech.cs6310.powergrid.domain.CustomerType;
import edu.gatech.cs6310.powergrid.domain.Location;
import edu.gatech.cs6310.powergrid.domain.PowerGridSystem;

class RatePlanServiceTest {

    private PowerGridSystem pgs;
    private CompanyService companies;
    private CustomerService customers;
    private RatePlanService plans;

    @BeforeEach
    void setup() {
        pgs = new PowerGridSystem(50, 25, 10);
        companies = new CompanyService(pgs);
        customers = new CustomerService(pgs);
        plans = new RatePlanService(pgs);
        companies.addCompany("Atlanta Power Co", "APC", new BigDecimal("0.20"));
    }

    @Test
    void fallsBackToStandardRateWhenNoPlanMatches() {
        Customer c = customers.createCustomer("APC", "Alice", CustomerType.RESIDENTIAL, new Location(0, 0));
        BigDecimal rate = plans.resolveRate(c, LocalDate.of(2026, 4, 21));
        assertThat(rate).isEqualByComparingTo("0.20");
    }

    @Test
    void accountSpecificPlanBeatsGenericPlan() {
        Customer c = customers.createCustomer("APC", "Alice", CustomerType.RESIDENTIAL, new Location(0, 0));
        plans.addRatePlan("GEN", "APC", new BigDecimal("0.15"),
            CustomerType.RESIDENTIAL, null,
            LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));
        plans.addRatePlan("VIP", "APC", new BigDecimal("0.08"),
            CustomerType.RESIDENTIAL, c.getAccountNumber(),
            LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));
        BigDecimal rate = plans.resolveRate(c, LocalDate.of(2026, 4, 21));
        assertThat(rate).isEqualByComparingTo("0.08");
    }

    @Test
    void genericPlanAppliesOutsideAccountSpecificWindow() {
        Customer c = customers.createCustomer("APC", "Alice", CustomerType.RESIDENTIAL, new Location(0, 0));
        plans.addRatePlan("GEN", "APC", new BigDecimal("0.15"),
            CustomerType.RESIDENTIAL, null,
            LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));
        plans.addRatePlan("VIP", "APC", new BigDecimal("0.08"),
            CustomerType.RESIDENTIAL, c.getAccountNumber(),
            LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 31));
        BigDecimal rate = plans.resolveRate(c, LocalDate.of(2026, 4, 21));
        assertThat(rate).isEqualByComparingTo("0.15");
    }

    @Test
    void customerTypeMustMatch() {
        Customer resi = customers.createCustomer("APC", "Alice", CustomerType.RESIDENTIAL, new Location(0, 0));
        plans.addRatePlan("COMM", "APC", new BigDecimal("0.09"),
            CustomerType.COMMERCIAL, null,
            LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));
        BigDecimal rate = plans.resolveRate(resi, LocalDate.of(2026, 4, 21));
        assertThat(rate).isEqualByComparingTo("0.20");
    }
}
