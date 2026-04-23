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
import edu.gatech.cs6310.powergrid.robustness.JournalCommand;
import edu.gatech.cs6310.powergrid.robustness.ProofService;
import edu.gatech.cs6310.powergrid.robustness.TransactionJournal;

@Service
public class UsageService {

    private final PowerGridSystem pgs;
    private final TransactionJournal journal;

    public UsageService(PowerGridSystem pgs, TransactionJournal journal) {
        this.pgs = pgs;
        this.journal = journal;
    }

    public LedgerEntry recordUsage(long accountNumber, LocalDate periodStart, LocalDate periodEnd, BigDecimal kWh) {
        ProofService.validatePositive("kWh", kWh);
        validatePeriod(periodStart, periodEnd);
        synchronized (pgs.lock()) {
            Customer customer = ProofService.validateExists("Customer", accountNumber, pgs.customers());
            long entryId = pgs.nextLedgerEntryId();
            JournalCommand.AddLedgerEntryCmd cmd = new JournalCommand.AddLedgerEntryCmd(
                entryId,
                customer.getCompanyShortName(),
                LedgerEntryType.USAGE,
                periodStart, periodEnd,
                kWh, null,
                "Usage for account " + accountNumber,
                Map.of("accountNumber", String.valueOf(accountNumber))
            );
            journal.append(cmd);
            cmd.apply(pgs);
            return pgs.ledger().get(pgs.ledger().size() - 1);
        }
    }

    public LedgerEntry recordProduction(String plantId, LocalDate periodStart, LocalDate periodEnd, BigDecimal kWh) {
        ProofService.validatePositive("kWh", kWh);
        validatePeriod(periodStart, periodEnd);
        synchronized (pgs.lock()) {
            PowerPlant plant = ProofService.validateExists("Power plant", plantId, pgs.plants());
            BigDecimal cost = plant.getGenerationCostPerKWh().multiply(kWh);

            long prodId = pgs.nextLedgerEntryId();
            JournalCommand.AddLedgerEntryCmd prodCmd = new JournalCommand.AddLedgerEntryCmd(
                prodId,
                plant.getCompanyShortName(),
                LedgerEntryType.PRODUCTION,
                periodStart, periodEnd,
                kWh, null,
                "Production by plant " + plantId,
                Map.of("plantId", plantId)
            );
            journal.append(prodCmd);
            prodCmd.apply(pgs);

            long costId = pgs.nextLedgerEntryId();
            JournalCommand.AddLedgerEntryCmd costCmd = new JournalCommand.AddLedgerEntryCmd(
                costId,
                plant.getCompanyShortName(),
                LedgerEntryType.GENERATION_COST,
                periodStart, periodEnd,
                kWh, cost,
                "Generation cost for plant " + plantId,
                Map.of("plantId", plantId)
            );
            journal.append(costCmd);
            costCmd.apply(pgs);

            int last = pgs.ledger().size();
            LedgerEntry production = null;
            for (LedgerEntry e : pgs.ledger()) {
                if (e.entryId() == prodId) { production = e; break; }
            }
            return production == null ? pgs.ledger().get(last - 2) : production;
        }
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
