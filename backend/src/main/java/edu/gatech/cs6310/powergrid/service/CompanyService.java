package edu.gatech.cs6310.powergrid.service;

import java.math.BigDecimal;
import java.util.Collection;

import org.springframework.stereotype.Service;

import edu.gatech.cs6310.powergrid.domain.PowerCompany;
import edu.gatech.cs6310.powergrid.domain.PowerGridSystem;
import edu.gatech.cs6310.powergrid.robustness.JournalCommand;
import edu.gatech.cs6310.powergrid.robustness.ProofService;
import edu.gatech.cs6310.powergrid.robustness.TransactionJournal;

@Service
public class CompanyService {

    private final PowerGridSystem pgs;
    private final TransactionJournal journal;

    public CompanyService(PowerGridSystem pgs, TransactionJournal journal) {
        this.pgs = pgs;
        this.journal = journal;
    }

    public PowerCompany addCompany(String longName, String shortName, BigDecimal standardRate) {
        ProofService.validateNotBlank("longName", longName);
        ProofService.validateNotBlank("shortName", shortName);
        ProofService.validatePositive("standardRate", standardRate);
        synchronized (pgs.lock()) {
            ProofService.validateUniqueShortName(shortName, pgs.companies().keySet());
            JournalCommand.AddCompanyCmd cmd = new JournalCommand.AddCompanyCmd(longName, shortName, standardRate);
            journal.append(cmd);
            cmd.apply(pgs);
            return pgs.companies().get(shortName);
        }
    }

    public PowerCompany updateStandardRate(String shortName, BigDecimal newRate) {
        ProofService.validatePositive("standardRate", newRate);
        synchronized (pgs.lock()) {
            PowerCompany c = ProofService.validateExists("Power company", shortName, pgs.companies());
            JournalCommand.UpdateStandardRateCmd cmd = new JournalCommand.UpdateStandardRateCmd(shortName, newRate);
            journal.append(cmd);
            cmd.apply(pgs);
            return c;
        }
    }

    public Collection<PowerCompany> list() {
        return pgs.companies().values();
    }

    public PowerCompany get(String shortName) {
        return ProofService.validateExists("Power company", shortName, pgs.companies());
    }
}
