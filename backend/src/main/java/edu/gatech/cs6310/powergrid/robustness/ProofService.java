package edu.gatech.cs6310.powergrid.robustness;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

import edu.gatech.cs6310.powergrid.domain.Location;
import edu.gatech.cs6310.powergrid.error.SystemError;

/**
 * Centralized pre-commit validation. Every service-layer mutation routes
 * capacity/distance/uniqueness/existence checks through these methods so the
 * rules live in exactly one place.
 */
public final class ProofService {

    private ProofService() {}

    public static void validateCapacity(String parentLabel, int currentCount, int max) {
        if (currentCount >= max) {
            throw SystemError.capacity(parentLabel, max);
        }
    }

    public static void validateDistance(Location from, Location to, int max, String relationship) {
        int actual = from.manhattanDistanceTo(to);
        if (actual > max) {
            throw SystemError.distance(actual, max, relationship);
        }
    }

    public static void validateUniqueShortName(String shortName, Set<String> existing) {
        if (existing.contains(shortName)) {
            throw SystemError.duplicate("Power company", shortName);
        }
    }

    public static <T> void validateAvailableId(String what, String id, Map<String, T> registry) {
        if (registry.containsKey(id)) {
            throw SystemError.duplicate(what, id);
        }
    }

    public static <T> T validateExists(String what, String id, Map<String, T> registry) {
        T value = registry.get(id);
        if (value == null) {
            throw SystemError.notFound(what, id);
        }
        return value;
    }

    public static <T> T validateExists(String what, long id, Map<Long, T> registry) {
        T value = registry.get(id);
        if (value == null) {
            throw SystemError.notFound(what, String.valueOf(id));
        }
        return value;
    }

    public static void validatePositive(String field, BigDecimal value) {
        if (value == null || value.signum() <= 0) {
            throw new SystemError(
                edu.gatech.cs6310.powergrid.error.ErrorCode.INVALID_RANGE,
                field + " must be greater than zero.",
                field,
                "Supply a positive numeric value."
            );
        }
    }

    public static void validateNonNegative(String field, BigDecimal value) {
        if (value == null || value.signum() < 0) {
            throw new SystemError(
                edu.gatech.cs6310.powergrid.error.ErrorCode.INVALID_RANGE,
                field + " must be zero or greater.",
                field,
                "Supply a non-negative numeric value."
            );
        }
    }

    public static void validateNotBlank(String field, String value) {
        if (value == null || value.isBlank()) {
            throw SystemError.invalidCommand(field + " is required.", field);
        }
    }
}
