package edu.gatech.cs6310.powergrid.domain;

import java.util.Optional;

public class Customer {

    private final long accountNumber;
    private final String companyShortName;
    private String name;
    private CustomerType customerType;
    private Location location;
    private String connectedTransformerId;

    public Customer(long accountNumber, String companyShortName, String name,
                    CustomerType customerType, Location location) {
        this.accountNumber = accountNumber;
        this.companyShortName = companyShortName;
        this.name = name;
        this.customerType = customerType;
        this.location = location;
    }

    public long getAccountNumber() { return accountNumber; }
    public String getCompanyShortName() { return companyShortName; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public CustomerType getCustomerType() { return customerType; }
    public void setCustomerType(CustomerType t) { this.customerType = t; }
    public Location getLocation() { return location; }
    public void setLocation(Location location) { this.location = location; }
    public Optional<String> getConnectedTransformerId() { return Optional.ofNullable(connectedTransformerId); }
    public void setConnectedTransformerId(String id) { this.connectedTransformerId = id; }
}
