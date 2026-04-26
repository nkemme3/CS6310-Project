package edu.gatech.cs6310.powergrid.error;

public class SystemError extends RuntimeException {

    private final ErrorCode code;
    private final String field;
    private final String remediation;

    public SystemError(ErrorCode code, String message, String field, String remediation) {
        super(message);
        this.code = code;
        this.field = field;
        this.remediation = remediation;
    }

    public SystemError(ErrorCode code, String message) {
        this(code, message, null, null);
    }

    public ErrorCode code() {
        return code;
    }

    public String field() {
        return field;
    }

    public String remediation() {
        return remediation;
    }

    public static SystemError notFound(String what, String id) {
        return new SystemError(
            ErrorCode.ENTITY_NOT_FOUND,
            what + " '" + id + "' was not found.",
            null,
            "Verify the identifier and confirm the entity has been created."
        );
    }

    public static SystemError duplicate(String what, String id) {
        return new SystemError(
            ErrorCode.DUPLICATE_ENTITY,
            what + " '" + id + "' already exists.",
            null,
            "Use a different identifier or remove the existing entity first."
        );
    }

    public static SystemError capacity(String parent, int max) {
        return new SystemError(
            ErrorCode.CAPACITY_EXCEEDED,
            parent + " has reached its maximum capacity of " + max + ".",
            null,
            "Remove an existing child or raise the capacity limit."
        );
    }

    public static SystemError distance(int actual, int max, String relationship) {
        return new SystemError(
            ErrorCode.DISTANCE_EXCEEDED,
            "Manhattan distance " + actual + " exceeds max " + max + " for " + relationship + ".",
            null,
            "Place the component within the allowed distance or relax the limit."
        );
    }

    public static SystemError invalidState(String message) {
        return new SystemError(ErrorCode.INVALID_STATE, message, null, null);
    }

    public static SystemError invalidCommand(String message, String field) {
        return new SystemError(ErrorCode.INVALID_COMMAND, message, field,
            "Correct the input and retry.");
    }
}
