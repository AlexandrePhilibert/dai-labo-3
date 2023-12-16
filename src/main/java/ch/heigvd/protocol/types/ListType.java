package ch.heigvd.protocol.types;

public enum ListType {
    AVERAGE("avg"),
    LAST("last");

    private final String repr;

    ListType(String repr) {
        this.repr = repr;
    }

    public String getRepresentation() {
        return repr;
    }

    public static ListType get(String representation) {
        return switch (representation) {
            case "avg" -> AVERAGE;
            case "last" -> LAST;
            default -> throw new RuntimeException("Unknown representation");
        };
    }
}
