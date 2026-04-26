package edu.gatech.cs6310.powergrid.auth;

import java.time.Instant;
import java.util.EnumSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class User {

    private final String username;
    private String passwordHash;
    private final Set<Role> roles;
    private final Instant createdAt;

    @JsonCreator
    public User(
        @JsonProperty("username") String username,
        @JsonProperty("passwordHash") String passwordHash,
        @JsonProperty("roles") Set<Role> roles,
        @JsonProperty("createdAt") Instant createdAt
    ) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.roles = roles == null ? EnumSet.noneOf(Role.class) : EnumSet.copyOf(roles);
        this.createdAt = createdAt == null ? Instant.now() : createdAt;
    }

    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public Set<Role> getRoles() { return roles; }
    public Instant getCreatedAt() { return createdAt; }

    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public boolean hasRole(Role role) { return roles.contains(role); }
}
