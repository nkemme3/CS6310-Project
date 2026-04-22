package edu.gatech.cs6310.powergrid.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.gatech.cs6310.powergrid.domain.Customer;
import edu.gatech.cs6310.powergrid.domain.CustomerType;
import edu.gatech.cs6310.powergrid.domain.LedgerEntry;
import edu.gatech.cs6310.powergrid.domain.LedgerEntryType;
import edu.gatech.cs6310.powergrid.domain.Location;
import edu.gatech.cs6310.powergrid.domain.PowerGridSystem;

class BillingServiceTest {

    private PowerGridSystem pgs;
    private CompanyService companies;
    private InfrastructureService infra;
    private CustomerService customers;
    private RatePlanService plans;
    private UsageService usage;
    private BillingService billing;

    @BeforeEach
    void setup() {
        pgs = new PowerGridSystem(50, 25, 10);
        companies = new CompanyService(pgs);
        infra = new InfrastructureService(pgs);
        customers = new CustomerService(pgs);
        plans = new RatePlanService(pgs);
        usage = new UsageService(pgs);
        billing = new BillingService(pgs, plans);

        companies.addCompany("Atlanta Power Co", "APC", new BigDecimal("0.20"));
        infra.addPlant("APC", "P1", new Location(0, 0), new BigDecimal("10000"), new BigDecimal("0.05"));
        infra.addSubstation("APC", "S1", new Location(5, 5), new BigDecimal("2000"), new BigDecimal("50"));
        infra.addTransformer("APC", "T1", new Location(10, 10), new BigDecimal("500"), new BigDecimal("5"));
        infra.connectPlantToSubstation("P1", "S1");
        infra.connectSubstationToTransformer("S1", "T1");
    }

    @Test
    void endToEndBillingProducesBillAndRevenue() {
        Customer alice = customers.createCustomer("APC", "Alice", CustomerType.RESIDENTIAL, new Location(12, 12));
        customers.connectCustomerToTransformer(alice.getAccountNumber(), "T1");

        LocalDate start = LocalDate.of(2026, 4, 1);
        LocalDate end = LocalDate.of(2026, 4, 30);

        usage.recordUsage(alice.getAccountNumber(), start, end, new BigDecimal("100"));
        usage.recordProduction("P1", start, end, new BigDecimal("100"));

        List<LedgerEntry> created = billing.runBillingCycle("APC", start, end);
        BigDecimal totalBill = created.stream()
            .filter(e -> e.entryType() == LedgerEntryType.BILL)
            .map(LedgerEntry::amount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertThat(totalBill).isEqualByComparingTo("20.00");

        BigDecimal totalRevenue = pgs.ledger().stream()
            .filter(e -> e.entryType() == LedgerEntryType.REVENUE)
            .map(LedgerEntry::amount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertThat(totalRevenue).isEqualByComparingTo("20.00");

        BigDecimal maintenance = pgs.ledger().stream()
            .filter(e -> e.entryType() == LedgerEntryType.MAINTENANCE_COST)
            .map(LedgerEntry::amount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertThat(maintenance).isEqualByComparingTo("55");

        BigDecimal generation = pgs.ledger().stream()
            .filter(e -> e.entryType() == LedgerEntryType.GENERATION_COST)
            .map(LedgerEntry::amount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertThat(generation).isEqualByComparingTo("5.00");
    }

    @Test
    void customerWithNoUsageProducesNoBill() {
        Customer alice = customers.createCustomer("APC", "Alice", CustomerType.RESIDENTIAL, new Location(12, 12));
        customers.connectCustomerToTransformer(alice.getAccountNumber(), "T1");

        List<LedgerEntry> created = billing.runBillingCycle("APC",
            LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 30));
        assertThat(created.stream().noneMatch(e -> e.entryType() == LedgerEntryType.BILL)).isTrue();
    }
}
