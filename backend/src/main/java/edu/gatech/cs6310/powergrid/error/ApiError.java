package edu.gatech.cs6310.powergrid.error;

import java.time.Instant;

public record ApiError(
    String code,
    String message,
    String field,
    String remediation,
    Instant timestamp
) {
    public static ApiError from(SystemError error) {
        return new ApiError(
            error.code().name(),
            error.getMessage(),
            error.field(),
            error.remediation(),
            Instant.now()
        );
    }
}
