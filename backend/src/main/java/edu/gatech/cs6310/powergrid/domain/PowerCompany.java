package edu.gatech.cs6310.powergrid.domain;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PowerCompany {

    private final String longName;
    private final String shortName;
    private BigDecimal standardRate;

    private final Set<String> plantIds = new LinkedHashSet<>();
    private final Set<String> substationIds = new LinkedHashSet<>();
    private final Set<String> transformerIds = new LinkedHashSet<>();
    private final Set<Long> customerAccountNumbers = new LinkedHashSet<>();
    private final Set<String> employeeIds = new LinkedHashSet<>();
    private final Set<String> ratePlanIds = new LinkedHashSet<>();

    @JsonCreator
    public PowerCompany(
        @JsonProperty("longName") String longName,
        @JsonProperty("shortName") String shortName,
        @JsonProperty("standardRate") BigDecimal standardRate
    ) {
        this.longName = longName;
        this.shortName = shortName;
        this.standardRate = standardRate;
    }

    public String getLongName() { return longName; }
    public String getShortName() { return shortName; }
    public BigDecimal getStandardRate() { return standardRate; }
    public void setStandardRate(BigDecimal v) { this.standardRate = v; }

    public Set<String> getPlantIds() { return plantIds; }
    public Set<String> getSubstationIds() { return substationIds; }
    public Set<String> getTransformerIds() { return transformerIds; }
    public Set<Long> getCustomerAccountNumbers() { return customerAccountNumbers; }
    public Set<String> getEmployeeIds() { return employeeIds; }
    public Set<String> getRatePlanIds() { return ratePlanIds; }
}
