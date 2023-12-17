package ch.heigvd.protocol.types;

public enum GetType {
    AVERAGE("avg"),
    LAST("last");

    private final String repr;

    GetType(String repr) {
        this.repr = repr;
    }

    public String getRepresentation() {
        return repr;
    }

    public static GetType get(String representation) {
        return switch (representation) {
            case "avg" -> AVERAGE;
            case "last" -> LAST;
            default -> throw new RuntimeException("Unknown representation");
        };
    }
}
