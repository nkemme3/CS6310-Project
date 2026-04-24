package edu.gatech.cs6310.powergrid.auth;

import java.time.Instant;
import java.util.Set;

/**
 * In-memory authenticated session. Sessions are not journaled — logging in
 * after a crash is cheap, and we do not want to replay bearer tokens.
 */
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
