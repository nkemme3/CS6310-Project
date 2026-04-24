package edu.gatech.cs6310.powergrid.auth;

/**
 * Five coarse roles for A3 auth/authz mod.
 * ADMIN: full access including user management.
 * OPERATOR: infrastructure, customers, employees (not billing, not user mgmt).
 * BILLING: billing cycle, usage/production records, reports.
 * FIELD: equipment issues (report/assign/resolve).
 * VIEWER: read-only access.
 */
public enum Role {
    ADMIN, OPERATOR, BILLING, FIELD, VIEWER;

    public String authority() {
        return "ROLE_" + name();
    }
}
