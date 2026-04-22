package edu.gatech.cs6310.powergrid.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import org.springframework.stereotype.Service;

import edu.gatech.cs6310.powergrid.domain.Customer;
import edu.gatech.cs6310.powergrid.domain.LedgerEntry;
import edu.gatech.cs6310.powergrid.domain.LedgerEntryType;
import edu.gatech.cs6310.powergrid.domain.PowerGridSystem;
import edu.gatech.cs6310.powergrid.domain.PowerPlant;
import edu.gatech.cs6310.powergrid.error.SystemError;
import edu.gatech.cs6310.powergrid.robustness.ProofService;

@Service
public class UsageService {

    private final PowerGridSystem pgs;

    public UsageService(PowerGridSystem pgs) {
        this.pgs = pgs;
    }

    public LedgerEntry recordUsage(long accountNumber, LocalDate periodStart, LocalDate periodEnd, BigDecimal kWh) {
        ProofService.validatePositive("kWh", kWh);
        validatePeriod(periodStart, periodEnd);
        Customer customer = ProofService.validateExists("Customer", accountNumber, pgs.customers());
        LedgerEntry entry = new LedgerEntry(
            pgs.nextLedgerEntryId(),
            customer.getCompanyShortName(),
            LedgerEntryType.USAGE,
            periodStart,
            periodEnd,
            kWh,
            null,
            "Usage for account " + accountNumber,
            Map.of("accountNumber", String.valueOf(accountNumber))
        );
        pgs.ledger().add(entry);
        return entry;
    }

    public LedgerEntry recordProduction(String plantId, LocalDate periodStart, LocalDate periodEnd, BigDecimal kWh) {
        ProofService.validatePositive("kWh", kWh);
        validatePeriod(periodStart, periodEnd);
        PowerPlant plant = ProofService.validateExists("Power plant", plantId, pgs.plants());
        BigDecimal cost = plant.getGenerationCostPerKWh().multiply(kWh);
        LedgerEntry production = new LedgerEntry(
            pgs.nextLedgerEntryId(),
            plant.getCompanyShortName(),
            LedgerEntryType.PRODUCTION,
            periodStart,
            periodEnd,
            kWh,
            null,
            "Production by plant " + plantId,
            Map.of("plantId", plantId)
        );
        pgs.ledger().add(production);
        LedgerEntry genCost = new LedgerEntry(
            pgs.nextLedgerEntryId(),
            plant.getCompanyShortName(),
            LedgerEntryType.GENERATION_COST,
            periodStart,
            periodEnd,
            kWh,
            cost,
            "Generation cost for plant " + plantId,
            Map.of("plantId", plantId)
        );
        pgs.ledger().add(genCost);
        return production;
    }

    private void validatePeriod(LocalDate start, LocalDate end) {
        if (start == null || end == null) {
            throw SystemError.invalidCommand("periodStart and periodEnd are required.", "periodStart");
        }
        if (end.isBefore(start)) {
            throw SystemError.invalidCommand("periodEnd must be on or after periodStart.", "periodEnd");
        }
    }
}
