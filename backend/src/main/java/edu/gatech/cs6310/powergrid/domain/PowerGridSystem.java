package edu.gatech.cs6310.powergrid.domain;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PowerGridSystem {

    private final Map<String, PowerCompany> companies = new LinkedHashMap<>();
    private final Map<String, PowerPlant> plants = new LinkedHashMap<>();
    private final Map<String, Substation> substations = new LinkedHashMap<>();
    private final Map<String, Transformer> transformers = new LinkedHashMap<>();
    private final Map<Long, Customer> customers = new LinkedHashMap<>();
    private final Map<String, Employee> employees = new LinkedHashMap<>();
    private final Map<Long, EquipmentIssue> issues = new LinkedHashMap<>();
    private final Map<String, RatePlan> ratePlans = new LinkedHashMap<>();
    private final List<LedgerEntry> ledger = new java.util.ArrayList<>();

    private final AtomicLong customerAccountSequence = new AtomicLong(1000);
    private final AtomicLong issueSequence = new AtomicLong(1);
    private final AtomicLong ledgerSequence = new AtomicLong(1);

    private final int maxPlantSubstationDistance;
    private final int maxSubstationTransformerDistance;
    private final int maxTransformerCustomerDistance;

    public PowerGridSystem(
        @Value("${powergrid.distances.max-plant-substation:50}") int maxPlantSubstation,
        @Value("${powergrid.distances.max-substation-transformer:25}") int maxSubstationTransformer,
        @Value("${powergrid.distances.max-transformer-customer:10}") int maxTransformerCustomer
    ) {
        this.maxPlantSubstationDistance = maxPlantSubstation;
        this.maxSubstationTransformerDistance = maxSubstationTransformer;
        this.maxTransformerCustomerDistance = maxTransformerCustomer;
    }

    public int getMaxPlantSubstationDistance() { return maxPlantSubstationDistance; }
    public int getMaxSubstationTransformerDistance() { return maxSubstationTransformerDistance; }
    public int getMaxTransformerCustomerDistance() { return maxTransformerCustomerDistance; }

    public Map<String, PowerCompany> companies() { return companies; }
    public Map<String, PowerPlant> plants() { return plants; }
    public Map<String, Substation> substations() { return substations; }
    public Map<String, Transformer> transformers() { return transformers; }
    public Map<Long, Customer> customers() { return customers; }
    public Map<String, Employee> employees() { return employees; }
    public Map<Long, EquipmentIssue> issues() { return issues; }
    public Map<String, RatePlan> ratePlans() { return ratePlans; }
    public List<LedgerEntry> ledger() { return ledger; }

    public Optional<PowerCompany> findCompany(String shortName) { return Optional.ofNullable(companies.get(shortName)); }
    public Optional<PowerPlant> findPlant(String id) { return Optional.ofNullable(plants.get(id)); }
    public Optional<Substation> findSubstation(String id) { return Optional.ofNullable(substations.get(id)); }
    public Optional<Transformer> findTransformer(String id) { return Optional.ofNullable(transformers.get(id)); }
    public Optional<Customer> findCustomer(long accountNumber) { return Optional.ofNullable(customers.get(accountNumber)); }
    public Optional<Employee> findEmployee(String id) { return Optional.ofNullable(employees.get(id)); }
    public Optional<EquipmentIssue> findIssue(long id) { return Optional.ofNullable(issues.get(id)); }
    public Optional<RatePlan> findRatePlan(String id) { return Optional.ofNullable(ratePlans.get(id)); }

    public long nextAccountNumber() { return customerAccountSequence.getAndIncrement(); }
    public long nextIssueId() { return issueSequence.getAndIncrement(); }
    public long nextLedgerEntryId() { return ledgerSequence.getAndIncrement(); }

    public Collection<LedgerEntry> ledgerFor(String companyShortName) {
        return ledger.stream().filter(e -> e.companyShortName().equals(companyShortName)).toList();
    }
}
