package edu.gatech.cs6310.powergrid.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

public class EquipmentIssue {

    private final long issueId;
    private final String companyShortName;
    private final AssetType assetType;
    private final String assetId;
    private final Instant reportedAt;
    private final BigDecimal hoursRequired;
    private final BigDecimal materialsCost;
    private IssueStatus status;
    private String assignedEmployeeId;
    private Instant resolvedAt;
    private BigDecimal resolutionCost;

    public EquipmentIssue(long issueId, String companyShortName, AssetType assetType, String assetId,
                          BigDecimal hoursRequired, BigDecimal materialsCost, Instant reportedAt) {
        this.issueId = issueId;
        this.companyShortName = companyShortName;
        this.assetType = assetType;
        this.assetId = assetId;
        this.hoursRequired = hoursRequired;
        this.materialsCost = materialsCost;
        this.reportedAt = reportedAt;
        this.status = IssueStatus.REPORTED;
    }

    public long getIssueId() { return issueId; }
    public String getCompanyShortName() { return companyShortName; }
    public AssetType getAssetType() { return assetType; }
    public String getAssetId() { return assetId; }
    public Instant getReportedAt() { return reportedAt; }
    public BigDecimal getHoursRequired() { return hoursRequired; }
    public BigDecimal getMaterialsCost() { return materialsCost; }
    public IssueStatus getStatus() { return status; }
    public Optional<String> getAssignedEmployeeId() { return Optional.ofNullable(assignedEmployeeId); }
    public Optional<Instant> getResolvedAt() { return Optional.ofNullable(resolvedAt); }
    public Optional<BigDecimal> getResolutionCost() { return Optional.ofNullable(resolutionCost); }

    public void assign(String employeeId) {
        this.assignedEmployeeId = employeeId;
        this.status = IssueStatus.ASSIGNED;
    }

    public void resolve(Instant when, BigDecimal totalCost) {
        this.resolvedAt = when;
        this.resolutionCost = totalCost;
        this.status = IssueStatus.RESOLVED;
    }
}
