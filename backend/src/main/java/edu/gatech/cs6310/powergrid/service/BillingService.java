package edu.gatech.cs6310.powergrid.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import edu.gatech.cs6310.powergrid.domain.Customer;
import edu.gatech.cs6310.powergrid.domain.LedgerEntry;
import edu.gatech.cs6310.powergrid.domain.LedgerEntryType;
import edu.gatech.cs6310.powergrid.domain.PowerCompany;
import edu.gatech.cs6310.powergrid.domain.PowerGridSystem;
import edu.gatech.cs6310.powergrid.domain.Substation;
import edu.gatech.cs6310.powergrid.domain.Transformer;
import edu.gatech.cs6310.powergrid.error.SystemError;
import edu.gatech.cs6310.powergrid.robustness.ProofService;

@Service
public class BillingService {

    private final PowerGridSystem pgs;
    private final RatePlanService ratePlanService;

    public BillingService(PowerGridSystem pgs, RatePlanService ratePlanService) {
        this.pgs = pgs;
        this.ratePlanService = ratePlanService;
    }

    public List<LedgerEntry> runBillingCycle(String companyShortName, LocalDate periodStart, LocalDate periodEnd) {
        if (periodStart == null || periodEnd == null) {
            throw SystemError.invalidCommand("periodStart and periodEnd are required.", "periodStart");
        }
        if (periodEnd.isBefore(periodStart)) {
            throw SystemError.invalidCommand("periodEnd must be on or after periodStart.", "periodEnd");
        }
        PowerCompany company = ProofService.validateExists("Power company", companyShortName, pgs.companies());
        List<LedgerEntry> created = new ArrayList<>();

        Map<Long, BigDecimal> usageByAccount = new HashMap<>();
        for (LedgerEntry e : pgs.ledger()) {
            if (e.entryType() != LedgerEntryType.USAGE) continue;
            if (!companyShortName.equals(e.companyShortName())) continue;
            if (e.periodStart().isBefore(periodStart) || e.periodEnd().isAfter(periodEnd)) continue;
            String account = e.relatedIds() == null ? null : e.relatedIds().get("accountNumber");
            if (account == null) continue;
            long accountNumber = Long.parseLong(account);
            usageByAccount.merge(accountNumber, e.kWh(), BigDecimal::add);
        }

        for (Long accountNumber : company.getCustomerAccountNumbers()) {
            BigDecimal kWh = usageByAccount.getOrDefault(accountNumber, BigDecimal.ZERO);
            if (kWh.signum() == 0) continue;
            Customer customer = pgs.customers().get(accountNumber);
            BigDecimal rate = ratePlanService.resolveRate(customer, periodEnd);
            BigDecimal amount = rate.multiply(kWh).setScale(2, RoundingMode.HALF_UP);
            LedgerEntry bill = new LedgerEntry(
                pgs.nextLedgerEntryId(),
                companyShortName,
                LedgerEntryType.BILL,
                periodStart,
                periodEnd,
                kWh,
                amount,
                "Bill for account " + accountNumber,
                Map.of("accountNumber", String.valueOf(accountNumber),
                       "ratePerKWh", rate.toPlainString())
            );
            pgs.ledger().add(bill);
            LedgerEntry revenue = new LedgerEntry(
                pgs.nextLedgerEntryId(),
                companyShortName,
                LedgerEntryType.REVENUE,
                periodStart,
                periodEnd,
                kWh,
                amount,
                "Revenue from account " + accountNumber,
                Map.of("accountNumber", String.valueOf(accountNumber))
            );
            pgs.ledger().add(revenue);
            created.add(bill);
            created.add(revenue);
        }

        for (String substationId : company.getSubstationIds()) {
            Substation sub = pgs.substations().get(substationId);
            if (sub.getMaintenanceCostPerCycle() == null || sub.getMaintenanceCostPerCycle().signum() == 0) continue;
            LedgerEntry m = new LedgerEntry(
                pgs.nextLedgerEntryId(),
                companyShortName,
                LedgerEntryType.MAINTENANCE_COST,
                periodStart,
                periodEnd,
                null,
                sub.getMaintenanceCostPerCycle(),
                "Maintenance for substation " + substationId,
                Map.of("substationId", substationId)
            );
            pgs.ledger().add(m);
            created.add(m);
        }
        for (String transformerId : company.getTransformerIds()) {
            Transformer t = pgs.transformers().get(transformerId);
            if (t.getMaintenanceCostPerCycle() == null || t.getMaintenanceCostPerCycle().signum() == 0) continue;
            LedgerEntry m = new LedgerEntry(
                pgs.nextLedgerEntryId(),
                companyShortName,
                LedgerEntryType.MAINTENANCE_COST,
                periodStart,
                periodEnd,
                null,
                t.getMaintenanceCostPerCycle(),
                "Maintenance for transformer " + transformerId,
                Map.of("transformerId", transformerId)
            );
            pgs.ledger().add(m);
            created.add(m);
        }

        return created;
    }
}
