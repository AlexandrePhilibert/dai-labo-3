package ch.heigvd.protocol.types;

public enum ErrorReason {
    INVALID_STATUS("invalid_status"),
    INVALID_TYPE("invalid_type");

    private final String value;

    ErrorReason(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static ErrorReason fromValue(String s) {
        return switch (s) {
            case "invalid_status" -> INVALID_STATUS;
            case "invalid_type" -> INVALID_TYPE;
            default -> throw new RuntimeException("Unknown ErrorReason");
        };
    }
}