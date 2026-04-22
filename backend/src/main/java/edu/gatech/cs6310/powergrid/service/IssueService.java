package edu.gatech.cs6310.powergrid.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;

import org.springframework.stereotype.Service;

import edu.gatech.cs6310.powergrid.domain.AssetType;
import edu.gatech.cs6310.powergrid.domain.Employee;
import edu.gatech.cs6310.powergrid.domain.EquipmentIssue;
import edu.gatech.cs6310.powergrid.domain.IssueStatus;
import edu.gatech.cs6310.powergrid.domain.LedgerEntry;
import edu.gatech.cs6310.powergrid.domain.LedgerEntryType;
import edu.gatech.cs6310.powergrid.domain.PowerGridSystem;
import edu.gatech.cs6310.powergrid.error.SystemError;
import edu.gatech.cs6310.powergrid.robustness.ProofService;

@Service
public class IssueService {

    private final PowerGridSystem pgs;

    public IssueService(PowerGridSystem pgs) {
        this.pgs = pgs;
    }

    public EquipmentIssue reportIssue(String companyShortName, AssetType assetType, String assetId,
                                      BigDecimal hoursRequired, BigDecimal materialsCost) {
        ProofService.validateExists("Power company", companyShortName, pgs.companies());
        if (assetType == null) {
            throw SystemError.invalidCommand("assetType is required.", "assetType");
        }
        ProofService.validateNotBlank("assetId", assetId);
        ProofService.validatePositive("hoursRequired", hoursRequired);
        ProofService.validateNonNegative("materialsCost", materialsCost);

        String ownerShortName = switch (assetType) {
            case PLANT -> ProofService.validateExists("Power plant", assetId, pgs.plants()).getCompanyShortName();
            case SUBSTATION -> ProofService.validateExists("Substation", assetId, pgs.substations()).getCompanyShortName();
            case TRANSFORMER -> ProofService.validateExists("Transformer", assetId, pgs.transformers()).getCompanyShortName();
        };
        if (!ownerShortName.equals(companyShortName)) {
            throw SystemError.invalidState("Asset '" + assetId + "' does not belong to company '" + companyShortName + "'.");
        }

        long id = pgs.nextIssueId();
        EquipmentIssue issue = new EquipmentIssue(id, companyShortName, assetType, assetId,
            hoursRequired, materialsCost, Instant.now());
        pgs.issues().put(id, issue);
        return issue;
    }

    public EquipmentIssue assignIssue(long issueId, String employeeId) {
        EquipmentIssue issue = ProofService.validateExists("Issue", issueId, pgs.issues());
        Employee employee = ProofService.validateExists("Employee", employeeId, pgs.employees());
        if (!employee.getCompanyShortName().equals(issue.getCompanyShortName())) {
            throw SystemError.invalidState("Employee and issue must belong to the same company.");
        }
        if (issue.getStatus() == IssueStatus.RESOLVED) {
            throw SystemError.invalidState("Issue " + issueId + " is already resolved.");
        }
        issue.assign(employeeId);
        return issue;
    }

    public EquipmentIssue resolveIssue(long issueId) {
        EquipmentIssue issue = ProofService.validateExists("Issue", issueId, pgs.issues());
        if (issue.getStatus() != IssueStatus.ASSIGNED) {
            throw SystemError.invalidState("Issue " + issueId + " must be assigned before resolution.");
        }
        String employeeId = issue.getAssignedEmployeeId()
            .orElseThrow(() -> SystemError.invalidState("Assigned issue is missing an employee."));
        Employee employee = ProofService.validateExists("Employee", employeeId, pgs.employees());
        BigDecimal laborCost = employee.getHourlyWage().multiply(issue.getHoursRequired());
        BigDecimal total = laborCost.add(issue.getMaterialsCost());
        issue.resolve(Instant.now(), total);

        LedgerEntry entry = new LedgerEntry(
            pgs.nextLedgerEntryId(),
            issue.getCompanyShortName(),
            LedgerEntryType.REPAIR_COST,
            java.time.LocalDate.now(),
            java.time.LocalDate.now(),
            null,
            total,
            "Repair of " + issue.getAssetType() + " " + issue.getAssetId(),
            Map.of("issueId", String.valueOf(issueId), "employeeId", employeeId)
        );
        pgs.ledger().add(entry);
        return issue;
    }

    public Collection<EquipmentIssue> list() { return pgs.issues().values(); }

    public EquipmentIssue get(long id) {
        return ProofService.validateExists("Issue", id, pgs.issues());
    }
}
