package edu.gatech.cs6310.powergrid.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.gatech.cs6310.powergrid.domain.EnergySourceType;
import edu.gatech.cs6310.powergrid.domain.Location;
import edu.gatech.cs6310.powergrid.domain.PowerGridSystem;
import edu.gatech.cs6310.powergrid.robustness.TransactionJournal;
import edu.gatech.cs6310.powergrid.service.ReportingService.SourceBreakdown;
import edu.gatech.cs6310.powergrid.service.ReportingService.SourceBreakdownRow;
import edu.gatech.cs6310.powergrid.testsupport.TestJournal;

class EnergySourceReportingTest {

    private PowerGridSystem pgs;
    private CompanyService companies;
    private InfrastructureService infra;
    private UsageService usage;
    private ReportingService reports;

    @BeforeEach
    void setup() {
        pgs = new PowerGridSystem(50, 25, 10);
        TransactionJournal journal = TestJournal.inTempDir();
        companies = new CompanyService(pgs, journal);
        infra = new InfrastructureService(pgs, journal);
        usage = new UsageService(pgs, journal);
        reports = new ReportingService(pgs);
        companies.addCompany("Atlanta Power Co", "APC", new BigDecimal("0.20"));
    }

    @Test
    void breaksProductionDownBySourceAndComputesCarbon() {
        infra.addPlant("APC", "SOLAR1", new Location(0, 0),
            new BigDecimal("10000"), new BigDecimal("0.05"), EnergySourceType.SOLAR);
        infra.addPlant("APC", "COAL1", new Location(1, 1),
            new BigDecimal("10000"), new BigDecimal("0.05"), EnergySourceType.COAL);

        LocalDate start = LocalDate.of(2026, 1, 1);
        LocalDate end   = LocalDate.of(2026, 1, 31);
        usage.recordProduction("SOLAR1", start, end, new BigDecimal("1000"));
        usage.recordProduction("COAL1",  start, end, new BigDecimal("2000"));

        SourceBreakdown b = reports.sourceBreakdown("APC", start, end);
        assertThat(b.totalKWh()).isEqualByComparingTo("3000");
        assertThat(b.renewableKWh()).isEqualByComparingTo("1000");
        // 1000 * 0.00 + 2000 * 0.90 = 1800
        assertThat(b.totalEstimatedCarbonKg()).isEqualByComparingTo("1800.00");

        SourceBreakdownRow solar = b.rows().stream()
            .filter(r -> r.source() == EnergySourceType.SOLAR).findFirst().orElseThrow();
        assertThat(solar.renewable()).isTrue();
        assertThat(solar.kWhProduced()).isEqualByComparingTo("1000");

        SourceBreakdownRow coal = b.rows().stream()
            .filter(r -> r.source() == EnergySourceType.COAL).findFirst().orElseThrow();
        assertThat(coal.renewable()).isFalse();
        assertThat(coal.estimatedCarbonKg()).isEqualByComparingTo("1800.00");
    }

    @Test
    void plantDefaultsToNaturalGasWhenSourceNotProvided() {
        infra.addPlant("APC", "P1", new Location(0, 0),
            new BigDecimal("10000"), new BigDecimal("0.05"));
        assertThat(pgs.plants().get("P1").getEnergySource()).isEqualTo(EnergySourceType.NATURAL_GAS);
    }
}
