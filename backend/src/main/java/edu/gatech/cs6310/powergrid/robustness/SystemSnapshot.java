package edu.gatech.cs6310.powergrid.robustness;

import java.util.Map;

import edu.gatech.cs6310.powergrid.domain.Customer;
import edu.gatech.cs6310.powergrid.domain.Employee;
import edu.gatech.cs6310.powergrid.domain.EquipmentIssue;
import edu.gatech.cs6310.powergrid.domain.LedgerEntry;
import edu.gatech.cs6310.powergrid.domain.PowerCompany;
import edu.gatech.cs6310.powergrid.domain.PowerPlant;
import edu.gatech.cs6310.powergrid.domain.RatePlan;
import edu.gatech.cs6310.powergrid.domain.Substation;
import edu.gatech.cs6310.powergrid.domain.Transformer;

/**
 * Immutable, serializable view of PowerGridSystem used for checkpoints.
 * Maps preserve insertion order when written/read through Jackson.
 */
public record SystemSnapshot(
    long version,
    Sequences sequences,
    Map<String, PowerCompany> companies,
    Map<String, PowerPlant> plants,
    Map<String, Substation> substations,
    Map<String, Transformer> transformers,
    Map<Long, Customer> customers,
    Map<String, Employee> employees,
    Map<Long, EquipmentIssue> issues,
    Map<String, RatePlan> ratePlans,
    java.util.List<LedgerEntry> ledger,
    Map<String, edu.gatech.cs6310.powergrid.auth.User> users
) {

    public record Sequences(long customerAccount, long issue, long ledger) {}

    public static final long CURRENT_VERSION = 1L;
}
