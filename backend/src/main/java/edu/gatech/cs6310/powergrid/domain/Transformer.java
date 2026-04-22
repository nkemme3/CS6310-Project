package edu.gatech.cs6310.powergrid.domain;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

public class Transformer {

    public static final int DEFAULT_MAX_CUSTOMERS = 5;

    private final String transformerId;
    private final String companyShortName;
    private Location location;
    private BigDecimal installCost;
    private BigDecimal maintenanceCostPerCycle;
    private int maxCustomers;
    private String sourceSubstationId;
    private final Set<Long> customerAccountNumbers = new LinkedHashSet<>();

    public Transformer(String transformerId, String companyShortName, Location location,
                       BigDecimal installCost, BigDecimal maintenanceCostPerCycle) {
        this.transformerId = transformerId;
        this.companyShortName = companyShortName;
        this.location = location;
        this.installCost = installCost;
        this.maintenanceCostPerCycle = maintenanceCostPerCycle;
        this.maxCustomers = DEFAULT_MAX_CUSTOMERS;
    }

    public String getTransformerId() { return transformerId; }
    public String getCompanyShortName() { return companyShortName; }
    public Location getLocation() { return location; }
    public void setLocation(Location location) { this.location = location; }
    public BigDecimal getInstallCost() { return installCost; }
    public BigDecimal getMaintenanceCostPerCycle() { return maintenanceCostPerCycle; }
    public void setMaintenanceCostPerCycle(BigDecimal v) { this.maintenanceCostPerCycle = v; }
    public int getMaxCustomers() { return maxCustomers; }
    public void setMaxCustomers(int v) { this.maxCustomers = v; }
    public Optional<String> getSourceSubstationId() { return Optional.ofNullable(sourceSubstationId); }
    public void setSourceSubstationId(String substationId) { this.sourceSubstationId = substationId; }
    public Set<Long> getCustomerAccountNumbers() { return customerAccountNumbers; }

    public int customerCount() { return customerAccountNumbers.size(); }
    public boolean hasCapacity() { return customerAccountNumbers.size() < maxCustomers; }
    public void attachCustomer(long accountNumber) { customerAccountNumbers.add(accountNumber); }
    public void detachCustomer(long accountNumber) { customerAccountNumbers.remove(accountNumber); }
}
