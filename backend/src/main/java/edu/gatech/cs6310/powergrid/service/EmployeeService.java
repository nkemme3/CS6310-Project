package edu.gatech.cs6310.powergrid.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;

import org.springframework.stereotype.Service;

import edu.gatech.cs6310.powergrid.domain.Employee;
import edu.gatech.cs6310.powergrid.domain.PowerGridSystem;
import edu.gatech.cs6310.powergrid.robustness.JournalCommand;
import edu.gatech.cs6310.powergrid.robustness.ProofService;
import edu.gatech.cs6310.powergrid.robustness.TransactionJournal;

@Service
public class EmployeeService {

    private final PowerGridSystem pgs;
    private final TransactionJournal journal;

    public EmployeeService(PowerGridSystem pgs, TransactionJournal journal) {
        this.pgs = pgs;
        this.journal = journal;
    }

    public Employee addEmployee(String companyShortName, String employeeId, String name,
                                LocalDate startDate, BigDecimal hourlyWage) {
        ProofService.validateNotBlank("employeeId", employeeId);
        ProofService.validateNotBlank("name", name);
        ProofService.validatePositive("hourlyWage", hourlyWage);
        LocalDate effectiveStart = startDate == null ? LocalDate.now() : startDate;
        synchronized (pgs.lock()) {
            ProofService.validateExists("Power company", companyShortName, pgs.companies());
            ProofService.validateAvailableId("Employee", employeeId, pgs.employees());
            JournalCommand.AddEmployeeCmd cmd = new JournalCommand.AddEmployeeCmd(
                companyShortName, employeeId, name, effectiveStart, hourlyWage);
            journal.append(cmd);
            cmd.apply(pgs);
            return pgs.employees().get(employeeId);
        }
    }

    public Collection<Employee> list() { return pgs.employees().values(); }

    public Employee get(String id) {
        return ProofService.validateExists("Employee", id, pgs.employees());
    }
}
