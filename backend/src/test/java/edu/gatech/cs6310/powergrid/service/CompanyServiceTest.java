package edu.gatech.cs6310.powergrid.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import edu.gatech.cs6310.powergrid.domain.PowerGridSystem;
import edu.gatech.cs6310.powergrid.error.ErrorCode;
import edu.gatech.cs6310.powergrid.error.SystemError;
import edu.gatech.cs6310.powergrid.testsupport.TestJournal;

class CompanyServiceTest {

    private CompanyService svc() {
        PowerGridSystem pgs = new PowerGridSystem(50, 25, 10);
        return new CompanyService(pgs, TestJournal.inTempDir());
    }

    @Test
    void duplicateShortNameRejected() {
        CompanyService svc = svc();
        svc.addCompany("Atlanta Power Co", "APC", new BigDecimal("0.12"));
        assertThatThrownBy(() -> svc.addCompany("Another", "APC", new BigDecimal("0.15")))
            .isInstanceOf(SystemError.class)
            .extracting(e -> ((SystemError) e).code())
            .isEqualTo(ErrorCode.DUPLICATE_ENTITY);
    }

    @Test
    void blankShortNameRejected() {
        CompanyService svc = svc();
        assertThatThrownBy(() -> svc.addCompany("Something", "   ", new BigDecimal("0.12")))
            .isInstanceOf(SystemError.class)
            .extracting(e -> ((SystemError) e).code())
            .isEqualTo(ErrorCode.INVALID_COMMAND);
    }

    @Test
    void nonPositiveRateRejected() {
        CompanyService svc = svc();
        assertThatThrownBy(() -> svc.addCompany("Atlanta", "APC", BigDecimal.ZERO))
            .isInstanceOf(SystemError.class)
            .extracting(e -> ((SystemError) e).code())
            .isEqualTo(ErrorCode.INVALID_RANGE);
    }
}
