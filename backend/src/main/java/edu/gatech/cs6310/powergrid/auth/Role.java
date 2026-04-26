package edu.gatech.cs6310.powergrid.auth;

public enum Role {
    ADMIN, OPERATOR, BILLING, FIELD, VIEWER;

    public String authority() {
        return "ROLE_" + name();
    }
}
