package edu.gatech.cs6310.powergrid.robustness;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import edu.gatech.cs6310.powergrid.domain.CustomerType;
import edu.gatech.cs6310.powergrid.domain.Location;
import edu.gatech.cs6310.powergrid.domain.PowerGridSystem;
import edu.gatech.cs6310.powergrid.service.CompanyService;
import edu.gatech.cs6310.powergrid.service.CustomerService;
import edu.gatech.cs6310.powergrid.service.InfrastructureService;
import edu.gatech.cs6310.powergrid.service.RatePlanService;

class CheckpointRoundtripTest {

    @Test
    void snapshotAndRestorePreservesEverything(@TempDir Path tmp) throws IOException {
        PowerGridSystem pgs = new PowerGridSystem(50, 25, 10);
        CheckpointManager cpm = new CheckpointManager(tmp.toString());
        TransactionJournal journal = new TransactionJournal(cpm);

        CompanyService companies = new CompanyService(pgs, journal);
        InfrastructureService infra = new InfrastructureService(pgs, journal);
        CustomerService customers = new CustomerService(pgs, journal);
        RatePlanService plans = new RatePlanService(pgs, journal);

        companies.addCompany("Atlanta Power Co", "APC", new BigDecimal("0.20"));
        infra.addPlant("APC", "P1", new Location(0, 0), new BigDecimal("10000"), new BigDecimal("0.05"));
        infra.addSubstation("APC", "S1", new Location(5, 5), new BigDecimal("2000"), new BigDecimal("50"));
        infra.addTransformer("APC", "T1", new Location(10, 10), new BigDecimal("500"), new BigDecimal("5"));
        infra.connectPlantToSubstation("P1", "S1");
        infra.connectSubstationToTransformer("S1", "T1");
        long acct = customers.createCustomer("APC", "Alice", CustomerType.RESIDENTIAL, new Location(12, 12)).getAccountNumber();
        customers.connectCustomerToTransformer(acct, "T1");
        plans.addRatePlan("VIP", "APC", new BigDecimal("0.08"), CustomerType.RESIDENTIAL, acct,
            LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));

        SystemSnapshot snap = pgs.toSnapshot();
        cpm.write(snap);
        assertThat(Files.exists(cpm.checkpointFile())).isTrue();

        PowerGridSystem rehydrated = new PowerGridSystem(50, 25, 10);
        SystemSnapshot fromDisk = cpm.read().orElseThrow();
        rehydrated.restoreFrom(fromDisk);

        assertThat(rehydrated.companies()).containsOnlyKeys("APC");
        assertThat(rehydrated.plants().get("P1").getSubstationIds()).contains("S1");
        assertThat(rehydrated.substations().get("S1").getSourcePlantId()).contains("P1");
        assertThat(rehydrated.substations().get("S1").getTransformerIds()).contains("T1");
        assertThat(rehydrated.transformers().get("T1").getSourceSubstationId()).contains("S1");
        assertThat(rehydrated.customers().get(acct).getName()).isEqualTo("Alice");
        assertThat(rehydrated.customers().get(acct).getConnectedTransformerId()).contains("T1");
        assertThat(rehydrated.ratePlans().get("VIP").getRatePerKWh()).isEqualByComparingTo("0.08");
        assertThat(rehydrated.nextAccountNumber()).isGreaterThan(acct);
    }
}
