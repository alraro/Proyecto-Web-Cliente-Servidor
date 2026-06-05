package es.grupo8.backend.exception;

public class ShiftConflictException extends RuntimeException {

    private final String conflictType;

    public ShiftConflictException(String message, String conflictType) {
        super(message);
        this.conflictType = conflictType;
    }

    public String getConflictType() {
        return conflictType;
    }
}
