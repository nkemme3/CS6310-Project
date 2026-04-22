package edu.gatech.cs6310.powergrid.service;

import java.math.BigDecimal;
import java.util.Collection;

import org.springframework.stereotype.Service;

import edu.gatech.cs6310.powergrid.domain.PowerCompany;
import edu.gatech.cs6310.powergrid.domain.PowerGridSystem;
import edu.gatech.cs6310.powergrid.robustness.ProofService;

@Service
public class CompanyService {

    private final PowerGridSystem pgs;

    public CompanyService(PowerGridSystem pgs) {
        this.pgs = pgs;
    }

    public PowerCompany addCompany(String longName, String shortName, BigDecimal standardRate) {
        ProofService.validateNotBlank("longName", longName);
        ProofService.validateNotBlank("shortName", shortName);
        ProofService.validatePositive("standardRate", standardRate);
        ProofService.validateUniqueShortName(shortName, pgs.companies().keySet());
        PowerCompany c = new PowerCompany(longName, shortName, standardRate);
        pgs.companies().put(shortName, c);
        return c;
    }

    public PowerCompany updateStandardRate(String shortName, BigDecimal newRate) {
        ProofService.validatePositive("standardRate", newRate);
        PowerCompany c = ProofService.validateExists("Power company", shortName, pgs.companies());
        c.setStandardRate(newRate);
        return c;
    }

    public Collection<PowerCompany> list() {
        return pgs.companies().values();
    }

    public PowerCompany get(String shortName) {
        return ProofService.validateExists("Power company", shortName, pgs.companies());
    }
}
