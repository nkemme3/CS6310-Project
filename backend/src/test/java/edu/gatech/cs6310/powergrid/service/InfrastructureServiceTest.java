package edu.gatech.cs6310.powergrid.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.gatech.cs6310.powergrid.domain.Location;
import edu.gatech.cs6310.powergrid.domain.PowerGridSystem;
import edu.gatech.cs6310.powergrid.error.ErrorCode;
import edu.gatech.cs6310.powergrid.error.SystemError;
import edu.gatech.cs6310.powergrid.robustness.TransactionJournal;
import edu.gatech.cs6310.powergrid.testsupport.TestJournal;

class InfrastructureServiceTest {

    private PowerGridSystem pgs;
    private CompanyService companies;
    private InfrastructureService infra;

    @BeforeEach
    void setup() {
        pgs = new PowerGridSystem(50, 25, 10);
        TransactionJournal journal = TestJournal.inTempDir();
        companies = new CompanyService(pgs, journal);
        infra = new InfrastructureService(pgs, journal);
        companies.addCompany("Atlanta Power Co", "APC", new BigDecimal("0.12"));
    }

    @Test
    void addsPlantAndTracksOnCompany() {
        infra.addPlant("APC", "P1", new Location(0, 0), new BigDecimal("1000"), new BigDecimal("0.05"));
        assertThat(pgs.plants()).containsKey("P1");
        assertThat(companies.get("APC").getPlantIds()).contains("P1");
    }

    @Test
    void duplicatePlantIdRejected() {
        infra.addPlant("APC", "P1", new Location(0, 0), new BigDecimal("1000"), new BigDecimal("0.05"));
        assertThatThrownBy(() ->
            infra.addPlant("APC", "P1", new Location(1, 1), new BigDecimal("1000"), new BigDecimal("0.05")))
            .isInstanceOf(SystemError.class)
            .extracting(e -> ((SystemError) e).code())
            .isEqualTo(ErrorCode.DUPLICATE_ENTITY);
    }

    @Test
    void connectsPlantToSubstationWhenWithinDistance() {
        infra.addPlant("APC", "P1", new Location(0, 0), new BigDecimal("1000"), new BigDecimal("0.05"));
        infra.addSubstation("APC", "S1", new Location(10, 10), new BigDecimal("500"), new BigDecimal("10"));
        infra.connectPlantToSubstation("P1", "S1");
        assertThat(pgs.substations().get("S1").getSourcePlantId()).contains("P1");
        assertThat(pgs.plants().get("P1").getSubstationIds()).contains("S1");
    }

    @Test
    void rejectsPlantSubstationConnectionOverDistance() {
        infra.addPlant("APC", "P1", new Location(0, 0), new BigDecimal("1000"), new BigDecimal("0.05"));
        infra.addSubstation("APC", "S1", new Location(100, 0), new BigDecimal("500"), new BigDecimal("10"));
        assertThatThrownBy(() -> infra.connectPlantToSubstation("P1", "S1"))
            .isInstanceOf(SystemError.class)
            .extracting(e -> ((SystemError) e).code())
            .isEqualTo(ErrorCode.DISTANCE_EXCEEDED);
    }

    @Test
    void rejectsDoubleSourcingASubstation() {
        infra.addPlant("APC", "P1", new Location(0, 0), new BigDecimal("1000"), new BigDecimal("0.05"));
        infra.addPlant("APC", "P2", new Location(1, 1), new BigDecimal("1000"), new BigDecimal("0.05"));
        infra.addSubstation("APC", "S1", new Location(5, 5), new BigDecimal("500"), new BigDecimal("10"));
        infra.connectPlantToSubstation("P1", "S1");
        assertThatThrownBy(() -> infra.connectPlantToSubstation("P2", "S1"))
            .isInstanceOf(SystemError.class)
            .extracting(e -> ((SystemError) e).code())
            .isEqualTo(ErrorCode.ALREADY_CONNECTED);
    }

    @Test
    void rejectsCrossCompanyConnection() {
        companies.addCompany("Georgia Power", "GP", new BigDecimal("0.11"));
        infra.addPlant("APC", "P1", new Location(0, 0), new BigDecimal("1000"), new BigDecimal("0.05"));
        infra.addSubstation("GP", "S1", new Location(5, 5), new BigDecimal("500"), new BigDecimal("10"));
        assertThatThrownBy(() -> infra.connectPlantToSubstation("P1", "S1"))
            .isInstanceOf(SystemError.class);
    }
}
