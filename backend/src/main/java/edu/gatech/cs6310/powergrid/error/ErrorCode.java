package edu.gatech.cs6310.powergrid.error;

public enum ErrorCode {
    INVALID_COMMAND(400, "The request was malformed or missing required fields."),
    ENTITY_NOT_FOUND(404, "The referenced entity does not exist."),
    DUPLICATE_ENTITY(409, "An entity with the same identifier already exists."),
    CAPACITY_EXCEEDED(409, "Adding this would exceed the parent's capacity limit."),
    DISTANCE_EXCEEDED(409, "Components are further apart than the maximum allowed distance."),
    INVALID_STATE(409, "The operation is not valid in the current state."),
    ALREADY_CONNECTED(409, "The downstream entity is already connected to another source."),
    INVALID_RANGE(400, "A numeric value falls outside the permitted range."),
    UNAUTHORIZED(401, "Authentication is required for this operation."),
    FORBIDDEN(403, "The current user is not permitted to perform this operation.");

    private final int httpStatus;
    private final String defaultMessage;

    ErrorCode(int httpStatus, String defaultMessage) {
        this.httpStatus = httpStatus;
        this.defaultMessage = defaultMessage;
    }

    public int httpStatus() {
        return httpStatus;
    }

    public String defaultMessage() {
        return defaultMessage;
    }
}
