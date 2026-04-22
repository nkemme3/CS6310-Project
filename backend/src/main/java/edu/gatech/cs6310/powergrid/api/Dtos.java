package edu.gatech.cs6310.powergrid.api;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.gatech.cs6310.powergrid.domain.AssetType;
import edu.gatech.cs6310.powergrid.domain.Customer;
import edu.gatech.cs6310.powergrid.domain.CustomerType;
import edu.gatech.cs6310.powergrid.domain.Employee;
import edu.gatech.cs6310.powergrid.domain.EquipmentIssue;
import edu.gatech.cs6310.powergrid.domain.IssueStatus;
import edu.gatech.cs6310.powergrid.domain.LedgerEntry;
import edu.gatech.cs6310.powergrid.domain.LedgerEntryType;
import edu.gatech.cs6310.powergrid.domain.Location;
import edu.gatech.cs6310.powergrid.domain.PowerCompany;
import edu.gatech.cs6310.powergrid.domain.PowerPlant;
import edu.gatech.cs6310.powergrid.domain.RatePlan;
import edu.gatech.cs6310.powergrid.domain.Substation;
import edu.gatech.cs6310.powergrid.domain.Transformer;

/**
 * Request/response records for the REST layer. Domain entities are not exposed
 * directly — every controller response maps to one of these view records.
 */
public final class Dtos {

    private Dtos() {}

    public record CreateCompanyRequest(String longName, String shortName, BigDecimal standardRate) {}
    public record UpdateStandardRateRequest(BigDecimal standardRate) {}
    public record CompanyView(
        String longName, String shortName, BigDecimal standardRate,
        Set<String> plantIds, Set<String> substationIds, Set<String> transformerIds,
        Set<Long> customerAccountNumbers, Set<String> employeeIds, Set<String> ratePlanIds
    ) {
        public static CompanyView from(PowerCompany c) {
            return new CompanyView(
                c.getLongName(), c.getShortName(), c.getStandardRate(),
                c.getPlantIds(), c.getSubstationIds(), c.getTransformerIds(),
                c.getCustomerAccountNumbers(), c.getEmployeeIds(), c.getRatePlanIds()
            );
        }
    }

    public record LocationDto(int x, int y) {
        public Location toDomain() { return new Location(x, y); }
        public static LocationDto from(Location l) { return new LocationDto(l.x(), l.y()); }
    }

    public record CreatePlantRequest(
        String companyShortName, String plantId, LocationDto location,
        BigDecimal buildCost, BigDecimal generationCostPerKWh
    ) {}
    public record CreateSubstationRequest(
        String companyShortName, String substationId, LocationDto location,
        BigDecimal buildCost, BigDecimal maintenanceCostPerCycle
    ) {}
    public record CreateTransformerRequest(
        String companyShortName, String transformerId, LocationDto location,
        BigDecimal installCost, BigDecimal maintenanceCostPerCycle
    ) {}
    public record ConnectRequest(String sourceId, String targetId) {}

    public record PlantView(
        String plantId, String companyShortName, LocationDto location,
        BigDecimal buildCost, BigDecimal generationCostPerKWh,
        int maxSubstations, Set<String> substationIds
    ) {
        public static PlantView from(PowerPlant p) {
            return new PlantView(
                p.getPlantId(), p.getCompanyShortName(), LocationDto.from(p.getLocation()),
                p.getBuildCost(), p.getGenerationCostPerKWh(),
                p.getMaxSubstations(), p.getSubstationIds()
            );
        }
    }
    public record SubstationView(
        String substationId, String companyShortName, LocationDto location,
        BigDecimal buildCost, BigDecimal maintenanceCostPerCycle,
        int maxTransformers, String sourcePlantId, Set<String> transformerIds
    ) {
        public static SubstationView from(Substation s) {
            return new SubstationView(
                s.getSubstationId(), s.getCompanyShortName(), LocationDto.from(s.getLocation()),
                s.getBuildCost(), s.getMaintenanceCostPerCycle(),
                s.getMaxTransformers(), s.getSourcePlantId().orElse(null), s.getTransformerIds()
            );
        }
    }
    public record TransformerView(
        String transformerId, String companyShortName, LocationDto location,
        BigDecimal installCost, BigDecimal maintenanceCostPerCycle,
        int maxCustomers, String sourceSubstationId, Set<Long> customerAccountNumbers
    ) {
        public static TransformerView from(Transformer t) {
            return new TransformerView(
                t.getTransformerId(), t.getCompanyShortName(), LocationDto.from(t.getLocation()),
                t.getInstallCost(), t.getMaintenanceCostPerCycle(),
                t.getMaxCustomers(), t.getSourceSubstationId().orElse(null), t.getCustomerAccountNumbers()
            );
        }
    }

    public record CreateCustomerRequest(
        String companyShortName, String name, CustomerType customerType, LocationDto location
    ) {}
    public record ConnectCustomerRequest(long accountNumber, String transformerId) {}
    public record CustomerView(
        long accountNumber, String companyShortName, String name,
        CustomerType customerType, LocationDto location, String connectedTransformerId
    ) {
        public static CustomerView from(Customer c) {
            return new CustomerView(
                c.getAccountNumber(), c.getCompanyShortName(), c.getName(),
                c.getCustomerType(), LocationDto.from(c.getLocation()),
                c.getConnectedTransformerId().orElse(null)
            );
        }
    }

    public record CreateEmployeeRequest(
        String companyShortName, String employeeId, String name,
        LocalDate startDate, BigDecimal hourlyWage
    ) {}
    public record EmployeeView(
        String employeeId, String companyShortName, String name,
        LocalDate startDate, BigDecimal hourlyWage
    ) {
        public static EmployeeView from(Employee e) {
            return new EmployeeView(
                e.getEmployeeId(), e.getCompanyShortName(), e.getName(),
                e.getStartDate(), e.getHourlyWage()
            );
        }
    }

    public record ReportIssueRequest(
        String companyShortName, AssetType assetType, String assetId,
        BigDecimal hoursRequired, BigDecimal materialsCost
    ) {}
    public record AssignIssueRequest(String employeeId) {}
    public record IssueView(
        long issueId, String companyShortName, AssetType assetType, String assetId,
        Instant reportedAt, BigDecimal hoursRequired, BigDecimal materialsCost,
        IssueStatus status, String assignedEmployeeId, Instant resolvedAt, BigDecimal resolutionCost
    ) {
        public static IssueView from(EquipmentIssue e) {
            return new IssueView(
                e.getIssueId(), e.getCompanyShortName(), e.getAssetType(), e.getAssetId(),
                e.getReportedAt(), e.getHoursRequired(), e.getMaterialsCost(),
                e.getStatus(), e.getAssignedEmployeeId().orElse(null),
                e.getResolvedAt().orElse(null), e.getResolutionCost().orElse(null)
            );
        }
    }

    public record CreateRatePlanRequest(
        String planId, String companyShortName, BigDecimal ratePerKWh,
        CustomerType customerType, Long accountNumber,
        LocalDate effectiveStart, LocalDate effectiveEnd
    ) {}
    public record RatePlanView(
        String planId, String companyShortName, BigDecimal ratePerKWh,
        CustomerType customerType, Long accountNumber,
        LocalDate effectiveStart, LocalDate effectiveEnd
    ) {
        public static RatePlanView from(RatePlan p) {
            return new RatePlanView(
                p.getPlanId(), p.getCompanyShortName(), p.getRatePerKWh(),
                p.getCustomerType(), p.getAccountNumber().orElse(null),
                p.getEffectiveStart(), p.getEffectiveEnd()
            );
        }
    }

    public record RecordUsageRequest(long accountNumber, LocalDate periodStart, LocalDate periodEnd, BigDecimal kWh) {}
    public record RecordProductionRequest(String plantId, LocalDate periodStart, LocalDate periodEnd, BigDecimal kWh) {}
    public record RunBillingRequest(String companyShortName, LocalDate periodStart, LocalDate periodEnd) {}

    public record LedgerEntryView(
        long entryId, String companyShortName, LedgerEntryType entryType,
        LocalDate periodStart, LocalDate periodEnd,
        BigDecimal kWh, BigDecimal amount, String description, Map<String, String> relatedIds
    ) {
        public static LedgerEntryView from(LedgerEntry e) {
            return new LedgerEntryView(
                e.entryId(), e.companyShortName(), e.entryType(),
                e.periodStart(), e.periodEnd(),
                e.kWh(), e.amount(), e.description(), e.relatedIds()
            );
        }
    }

    public record BillingRunResponse(List<LedgerEntryView> entries) {
        public static BillingRunResponse of(List<LedgerEntry> entries) {
            return new BillingRunResponse(entries.stream().map(LedgerEntryView::from).toList());
        }
    }
}
