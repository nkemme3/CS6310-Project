package edu.gatech.cs6310.powergrid.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import edu.gatech.cs6310.powergrid.domain.EnergySourceType;
import edu.gatech.cs6310.powergrid.domain.LedgerEntry;
import edu.gatech.cs6310.powergrid.domain.LedgerEntryType;
import edu.gatech.cs6310.powergrid.domain.PowerGridSystem;
import edu.gatech.cs6310.powergrid.domain.PowerPlant;
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

    public record SourceBreakdownRow(
        EnergySourceType source,
        boolean renewable,
        int plantCount,
        BigDecimal kWhProduced,
        BigDecimal estimatedCarbonKg
    ) {}

    public record SourceBreakdown(
        String companyShortName,
        BigDecimal totalKWh,
        BigDecimal totalEstimatedCarbonKg,
        BigDecimal renewableKWh,
        List<SourceBreakdownRow> rows
    ) {}

    public SourceBreakdown sourceBreakdown(String companyShortName, LocalDate periodStart, LocalDate periodEnd) {
        ProofService.validateExists("Power company", companyShortName, pgs.companies());

        EnumMap<EnergySourceType, BigDecimal> kWhBySource = new EnumMap<>(EnergySourceType.class);
        EnumMap<EnergySourceType, Integer> plantsBySource = new EnumMap<>(EnergySourceType.class);
        for (PowerPlant p : pgs.plants().values()) {
            if (!p.getCompanyShortName().equals(companyShortName)) continue;
            plantsBySource.merge(p.getEnergySource(), 1, Integer::sum);
        }

        for (LedgerEntry e : pgs.ledger()) {
            if (!e.companyShortName().equals(companyShortName)) continue;
            if (e.entryType() != LedgerEntryType.PRODUCTION) continue;
            if (periodStart != null && e.periodEnd().isBefore(periodStart)) continue;
            if (periodEnd != null && e.periodStart().isAfter(periodEnd)) continue;
            String plantId = e.relatedIds() == null ? null : e.relatedIds().get("plantId");
            if (plantId == null) continue;
            PowerPlant plant = pgs.plants().get(plantId);
            if (plant == null) continue;
            BigDecimal kWh = e.kWh() == null ? BigDecimal.ZERO : e.kWh();
            kWhBySource.merge(plant.getEnergySource(), kWh, BigDecimal::add);
        }

        BigDecimal totalKWh = BigDecimal.ZERO;
        BigDecimal totalCarbon = BigDecimal.ZERO;
        BigDecimal renewableKWh = BigDecimal.ZERO;
        List<SourceBreakdownRow> rows = new java.util.ArrayList<>();
        for (EnergySourceType src : EnergySourceType.values()) {
            BigDecimal kWh = kWhBySource.getOrDefault(src, BigDecimal.ZERO);
            int count = plantsBySource.getOrDefault(src, 0);
            if (kWh.signum() == 0 && count == 0) continue;
            BigDecimal carbon = src.carbonFactorKgPerKWh().multiply(kWh);
            rows.add(new SourceBreakdownRow(src, src.isRenewable(), count, kWh, carbon));
            totalKWh = totalKWh.add(kWh);
            totalCarbon = totalCarbon.add(carbon);
            if (src.isRenewable()) renewableKWh = renewableKWh.add(kWh);
        }
        return new SourceBreakdown(companyShortName, totalKWh, totalCarbon, renewableKWh, rows);
    }

    @SuppressWarnings("unused")
    private static Map<EnergySourceType, BigDecimal> emptyTyped() { return new EnumMap<>(EnergySourceType.class); }
}
