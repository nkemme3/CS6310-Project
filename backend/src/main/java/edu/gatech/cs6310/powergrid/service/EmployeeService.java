package edu.gatech.cs6310.powergrid.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;

import org.springframework.stereotype.Service;

import edu.gatech.cs6310.powergrid.domain.Employee;
import edu.gatech.cs6310.powergrid.domain.PowerCompany;
import edu.gatech.cs6310.powergrid.domain.PowerGridSystem;
import edu.gatech.cs6310.powergrid.robustness.ProofService;

@Service
public class EmployeeService {

    private final PowerGridSystem pgs;

    public EmployeeService(PowerGridSystem pgs) {
        this.pgs = pgs;
    }

    public Employee addEmployee(String companyShortName, String employeeId, String name,
                                LocalDate startDate, BigDecimal hourlyWage) {
        ProofService.validateNotBlank("employeeId", employeeId);
        ProofService.validateNotBlank("name", name);
        ProofService.validatePositive("hourlyWage", hourlyWage);
        PowerCompany company = ProofService.validateExists("Power company", companyShortName, pgs.companies());
        ProofService.validateAvailableId("Employee", employeeId, pgs.employees());
        Employee e = new Employee(employeeId, companyShortName, name,
            startDate == null ? LocalDate.now() : startDate, hourlyWage);
        pgs.employees().put(employeeId, e);
        company.getEmployeeIds().add(employeeId);
        return e;
    }

    public Collection<Employee> list() { return pgs.employees().values(); }

    public Employee get(String id) {
        return ProofService.validateExists("Employee", id, pgs.employees());
    }
}
