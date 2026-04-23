package edu.gatech.cs6310.powergrid.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

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

    @JsonCreator
    public EquipmentIssue(
        @JsonProperty("issueId") long issueId,
        @JsonProperty("companyShortName") String companyShortName,
        @JsonProperty("assetType") AssetType assetType,
        @JsonProperty("assetId") String assetId,
        @JsonProperty("hoursRequired") BigDecimal hoursRequired,
        @JsonProperty("materialsCost") BigDecimal materialsCost,
        @JsonProperty("reportedAt") Instant reportedAt
    ) {
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
    public void setStatus(IssueStatus status) { this.status = status; }
    @JsonIgnore
    public Optional<String> getAssignedEmployeeId() { return Optional.ofNullable(assignedEmployeeId); }
    @JsonIgnore
    public Optional<Instant> getResolvedAt() { return Optional.ofNullable(resolvedAt); }
    @JsonIgnore
    public Optional<BigDecimal> getResolutionCost() { return Optional.ofNullable(resolutionCost); }

    public void setAssignedEmployeeId(String id) { this.assignedEmployeeId = id; }
    public void setResolvedAt(Instant resolvedAt) { this.resolvedAt = resolvedAt; }
    public void setResolutionCost(BigDecimal cost) { this.resolutionCost = cost; }

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
