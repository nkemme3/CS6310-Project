package edu.gatech.cs6310.powergrid.robustness;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import edu.gatech.cs6310.powergrid.auth.Role;
import edu.gatech.cs6310.powergrid.auth.User;
import edu.gatech.cs6310.powergrid.domain.AssetType;
import edu.gatech.cs6310.powergrid.domain.Customer;
import edu.gatech.cs6310.powergrid.domain.CustomerType;
import edu.gatech.cs6310.powergrid.domain.Employee;
import edu.gatech.cs6310.powergrid.domain.EnergySourceType;
import edu.gatech.cs6310.powergrid.domain.EquipmentIssue;
import edu.gatech.cs6310.powergrid.domain.IssueStatus;
import edu.gatech.cs6310.powergrid.domain.LedgerEntry;
import edu.gatech.cs6310.powergrid.domain.LedgerEntryType;
import edu.gatech.cs6310.powergrid.domain.Location;
import edu.gatech.cs6310.powergrid.domain.PowerCompany;
import edu.gatech.cs6310.powergrid.domain.PowerGridSystem;
import edu.gatech.cs6310.powergrid.domain.PowerPlant;
import edu.gatech.cs6310.powergrid.domain.RatePlan;
import edu.gatech.cs6310.powergrid.domain.Substation;
import edu.gatech.cs6310.powergrid.domain.Transformer;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = JournalCommand.AddCompanyCmd.class,              name = "AddCompany"),
    @JsonSubTypes.Type(value = JournalCommand.UpdateStandardRateCmd.class,      name = "UpdateStandardRate"),
    @JsonSubTypes.Type(value = JournalCommand.AddPlantCmd.class,                name = "AddPlant"),
    @JsonSubTypes.Type(value = JournalCommand.AddSubstationCmd.class,           name = "AddSubstation"),
    @JsonSubTypes.Type(value = JournalCommand.AddTransformerCmd.class,          name = "AddTransformer"),
    @JsonSubTypes.Type(value = JournalCommand.ConnectPlantSubstationCmd.class,  name = "ConnectPlantSubstation"),
    @JsonSubTypes.Type(value = JournalCommand.ConnectSubstationTransformerCmd.class, name = "ConnectSubstationTransformer"),
    @JsonSubTypes.Type(value = JournalCommand.CreateCustomerCmd.class,          name = "CreateCustomer"),
    @JsonSubTypes.Type(value = JournalCommand.ConnectCustomerTransformerCmd.class, name = "ConnectCustomerTransformer"),
    @JsonSubTypes.Type(value = JournalCommand.AddEmployeeCmd.class,             name = "AddEmployee"),
    @JsonSubTypes.Type(value = JournalCommand.ReportIssueCmd.class,             name = "ReportIssue"),
    @JsonSubTypes.Type(value = JournalCommand.AssignIssueCmd.class,             name = "AssignIssue"),
    @JsonSubTypes.Type(value = JournalCommand.ResolveIssueCmd.class,            name = "ResolveIssue"),
    @JsonSubTypes.Type(value = JournalCommand.AddRatePlanCmd.class,             name = "AddRatePlan"),
    @JsonSubTypes.Type(value = JournalCommand.AddLedgerEntryCmd.class,          name = "AddLedgerEntry"),
    @JsonSubTypes.Type(value = JournalCommand.CreateUserCmd.class,              name = "CreateUser")
})
public sealed interface JournalCommand permits
    JournalCommand.AddCompanyCmd,
    JournalCommand.UpdateStandardRateCmd,
    JournalCommand.AddPlantCmd,
    JournalCommand.AddSubstationCmd,
    JournalCommand.AddTransformerCmd,
    JournalCommand.ConnectPlantSubstationCmd,
    JournalCommand.ConnectSubstationTransformerCmd,
    JournalCommand.CreateCustomerCmd,
    JournalCommand.ConnectCustomerTransformerCmd,
    JournalCommand.AddEmployeeCmd,
    JournalCommand.ReportIssueCmd,
    JournalCommand.AssignIssueCmd,
    JournalCommand.ResolveIssueCmd,
    JournalCommand.AddRatePlanCmd,
    JournalCommand.AddLedgerEntryCmd,
    JournalCommand.CreateUserCmd {

    void apply(PowerGridSystem pgs);

    record AddCompanyCmd(String longName, String shortName, BigDecimal standardRate) implements JournalCommand {
        @Override
        public void apply(PowerGridSystem pgs) {
            pgs.companies().put(shortName, new PowerCompany(longName, shortName, standardRate));
        }
    }

    record UpdateStandardRateCmd(String shortName, BigDecimal newRate) implements JournalCommand {
        @Override
        public void apply(PowerGridSystem pgs) {
            PowerCompany c = pgs.companies().get(shortName);
            if (c != null) c.setStandardRate(newRate);
        }
    }

    record AddPlantCmd(String companyShortName, String plantId, Location location,
                       BigDecimal buildCost, BigDecimal generationCostPerKWh,
                       EnergySourceType energySource) implements JournalCommand {
        @Override
        public void apply(PowerGridSystem pgs) {
            PowerPlant p = new PowerPlant(plantId, companyShortName, location, buildCost,
                generationCostPerKWh, energySource);
            pgs.plants().put(plantId, p);
            PowerCompany c = pgs.companies().get(companyShortName);
            if (c != null) c.getPlantIds().add(plantId);
        }
    }

    record AddSubstationCmd(String companyShortName, String substationId, Location location,
                            BigDecimal buildCost, BigDecimal maintenanceCostPerCycle) implements JournalCommand {
        @Override
        public void apply(PowerGridSystem pgs) {
            Substation s = new Substation(substationId, companyShortName, location, buildCost, maintenanceCostPerCycle);
            pgs.substations().put(substationId, s);
            PowerCompany c = pgs.companies().get(companyShortName);
            if (c != null) c.getSubstationIds().add(substationId);
        }
    }

    record AddTransformerCmd(String companyShortName, String transformerId, Location location,
                             BigDecimal installCost, BigDecimal maintenanceCostPerCycle) implements JournalCommand {
        @Override
        public void apply(PowerGridSystem pgs) {
            Transformer t = new Transformer(transformerId, companyShortName, location, installCost, maintenanceCostPerCycle);
            pgs.transformers().put(transformerId, t);
            PowerCompany c = pgs.companies().get(companyShortName);
            if (c != null) c.getTransformerIds().add(transformerId);
        }
    }

    record ConnectPlantSubstationCmd(String plantId, String substationId) implements JournalCommand {
        @Override
        public void apply(PowerGridSystem pgs) {
            PowerPlant plant = pgs.plants().get(plantId);
            Substation sub = pgs.substations().get(substationId);
            if (plant != null && sub != null) {
                plant.attachSubstation(substationId);
                sub.setSourcePlantId(plantId);
            }
        }
    }

    record ConnectSubstationTransformerCmd(String substationId, String transformerId) implements JournalCommand {
        @Override
        public void apply(PowerGridSystem pgs) {
            Substation sub = pgs.substations().get(substationId);
            Transformer t = pgs.transformers().get(transformerId);
            if (sub != null && t != null) {
                sub.attachTransformer(transformerId);
                t.setSourceSubstationId(substationId);
            }
        }
    }

    record CreateCustomerCmd(long accountNumber, String companyShortName, String name,
                             CustomerType customerType, Location location) implements JournalCommand {
        @Override
        public void apply(PowerGridSystem pgs) {
            Customer c = new Customer(accountNumber, companyShortName, name, customerType, location);
            pgs.customers().put(accountNumber, c);
            PowerCompany co = pgs.companies().get(companyShortName);
            if (co != null) co.getCustomerAccountNumbers().add(accountNumber);
            pgs.bumpAccountSequenceIfNeeded(accountNumber);
        }
    }

    record ConnectCustomerTransformerCmd(long accountNumber, String transformerId) implements JournalCommand {
        @Override
        public void apply(PowerGridSystem pgs) {
            Customer c = pgs.customers().get(accountNumber);
            Transformer t = pgs.transformers().get(transformerId);
            if (c != null && t != null) {
                t.attachCustomer(accountNumber);
                c.setConnectedTransformerId(transformerId);
            }
        }
    }

    record AddEmployeeCmd(String companyShortName, String employeeId, String name,
                          LocalDate startDate, BigDecimal hourlyWage) implements JournalCommand {
        @Override
        public void apply(PowerGridSystem pgs) {
            Employee e = new Employee(employeeId, companyShortName, name, startDate, hourlyWage);
            pgs.employees().put(employeeId, e);
            PowerCompany c = pgs.companies().get(companyShortName);
            if (c != null) c.getEmployeeIds().add(employeeId);
        }
    }

    record ReportIssueCmd(long issueId, String companyShortName, AssetType assetType, String assetId,
                          BigDecimal hoursRequired, BigDecimal materialsCost, Instant reportedAt) implements JournalCommand {
        @Override
        public void apply(PowerGridSystem pgs) {
            EquipmentIssue issue = new EquipmentIssue(issueId, companyShortName, assetType, assetId,
                hoursRequired, materialsCost, reportedAt);
            pgs.issues().put(issueId, issue);
            pgs.bumpIssueSequenceIfNeeded(issueId);
        }
    }

    record AssignIssueCmd(long issueId, String employeeId) implements JournalCommand {
        @Override
        public void apply(PowerGridSystem pgs) {
            EquipmentIssue i = pgs.issues().get(issueId);
            if (i != null) {
                i.setAssignedEmployeeId(employeeId);
                i.setStatus(IssueStatus.ASSIGNED);
            }
        }
    }

    record ResolveIssueCmd(long issueId, Instant resolvedAt, BigDecimal resolutionCost) implements JournalCommand {
        @Override
        public void apply(PowerGridSystem pgs) {
            EquipmentIssue i = pgs.issues().get(issueId);
            if (i != null) {
                i.setResolvedAt(resolvedAt);
                i.setResolutionCost(resolutionCost);
                i.setStatus(IssueStatus.RESOLVED);
            }
        }
    }

    record AddRatePlanCmd(String planId, String companyShortName, BigDecimal ratePerKWh,
                          CustomerType customerType, Long accountNumber,
                          LocalDate effectiveStart, LocalDate effectiveEnd) implements JournalCommand {
        @Override
        public void apply(PowerGridSystem pgs) {
            RatePlan plan = new RatePlan(planId, companyShortName, ratePerKWh, customerType,
                accountNumber, effectiveStart, effectiveEnd);
            pgs.ratePlans().put(planId, plan);
            PowerCompany c = pgs.companies().get(companyShortName);
            if (c != null) c.getRatePlanIds().add(planId);
        }
    }

    record AddLedgerEntryCmd(long entryId, String companyShortName, LedgerEntryType entryType,
                             LocalDate periodStart, LocalDate periodEnd, BigDecimal kWh,
                             BigDecimal amount, String description, Map<String, String> relatedIds) implements JournalCommand {
        @Override
        public void apply(PowerGridSystem pgs) {
            pgs.ledger().add(new LedgerEntry(entryId, companyShortName, entryType, periodStart, periodEnd,
                kWh, amount, description, relatedIds));
            pgs.bumpLedgerSequenceIfNeeded(entryId);
        }
    }

    record CreateUserCmd(String username, String passwordHash, Set<Role> roles, Instant createdAt) implements JournalCommand {
        @Override
        public void apply(PowerGridSystem pgs) {
            pgs.users().put(username, new User(username, passwordHash, roles, createdAt));
        }
    }
}
