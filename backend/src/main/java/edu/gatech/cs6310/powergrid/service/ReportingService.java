package edu.gatech.cs6310.powergrid.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import edu.gatech.cs6310.powergrid.domain.LedgerEntry;
import edu.gatech.cs6310.powergrid.domain.LedgerEntryType;
import edu.gatech.cs6310.powergrid.domain.PowerGridSystem;
import edu.gatech.cs6310.powergrid.robustness.ProofService;

@Service
public class ReportingService {

    private final PowerGridSystem pgs;

    public ReportingService(PowerGridSystem pgs) {
        this.pgs = pgs;
    }

    public record CompanySummary(
        String companyShortName,
        BigDecimal totalRevenue,
        BigDecimal totalCost,
        BigDecimal netIncome,
        BigDecimal totalKWhProduced,
        BigDecimal totalKWhBilled
    ) {}

    public CompanySummary summarize(String companyShortName, LocalDate periodStart, LocalDate periodEnd) {
        ProofService.validateExists("Power company", companyShortName, pgs.companies());
        BigDecimal revenue = BigDecimal.ZERO;
        BigDecimal cost = BigDecimal.ZERO;
        BigDecimal produced = BigDecimal.ZERO;
        BigDecimal billed = BigDecimal.ZERO;
        for (LedgerEntry e : pgs.ledger()) {
            if (!e.companyShortName().equals(companyShortName)) continue;
            if (periodStart != null && e.periodEnd().isBefore(periodStart)) continue;
            if (periodEnd != null && e.periodStart().isAfter(periodEnd)) continue;
            BigDecimal amount = e.amount() == null ? BigDecimal.ZERO : e.amount();
            BigDecimal kWh = e.kWh() == null ? BigDecimal.ZERO : e.kWh();
            switch (e.entryType()) {
                case REVENUE -> revenue = revenue.add(amount);
                case GENERATION_COST, MAINTENANCE_COST, REPAIR_COST -> cost = cost.add(amount);
                case PRODUCTION -> produced = produced.add(kWh);
                case BILL -> billed = billed.add(kWh);
                default -> { }
            }
        }
        return new CompanySummary(companyShortName, revenue, cost, revenue.subtract(cost), produced, billed);
    }

    public List<LedgerEntry> ledgerFor(String companyShortName) {
        ProofService.validateExists("Power company", companyShortName, pgs.companies());
        return pgs.ledger().stream()
            .filter(e -> e.companyShortName().equals(companyShortName))
            .toList();
    }

    public List<LedgerEntry> ledgerFor(String companyShortName, LedgerEntryType type) {
        ProofService.validateExists("Power company", companyShortName, pgs.companies());
        return pgs.ledger().stream()
            .filter(e -> e.companyShortName().equals(companyShortName))
            .filter(e -> e.entryType() == type)
            .toList();
    }
}
