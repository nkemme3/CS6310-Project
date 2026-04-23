package edu.gatech.cs6310.powergrid.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Employee {

    private final String employeeId;
    private final String companyShortName;
    private String name;
    private LocalDate startDate;
    private BigDecimal hourlyWage;

    @JsonCreator
    public Employee(
        @JsonProperty("employeeId") String employeeId,
        @JsonProperty("companyShortName") String companyShortName,
        @JsonProperty("name") String name,
        @JsonProperty("startDate") LocalDate startDate,
        @JsonProperty("hourlyWage") BigDecimal hourlyWage
    ) {
        this.employeeId = employeeId;
        this.companyShortName = companyShortName;
        this.name = name;
        this.startDate = startDate;
        this.hourlyWage = hourlyWage;
    }

    public String getEmployeeId() { return employeeId; }
    public String getCompanyShortName() { return companyShortName; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate d) { this.startDate = d; }
    public BigDecimal getHourlyWage() { return hourlyWage; }
    public void setHourlyWage(BigDecimal w) { this.hourlyWage = w; }
}
