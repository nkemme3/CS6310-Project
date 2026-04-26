package edu.gatech.cs6310.powergrid.auth;

import java.time.Instant;
import java.util.Set;

public record Session(
    String token,
    String username,
    Set<Role> roles,
    Instant issuedAt,
    Instant expiresAt
) {
    public boolean isExpired(Instant now) {
        return now.isAfter(expiresAt);
    }
}
