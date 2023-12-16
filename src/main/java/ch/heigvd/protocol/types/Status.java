package ch.heigvd.protocol.types;

public enum Status {
    UP(true),
    DOWN(false);

    private final boolean booleanRepresentation;

    Status(boolean booleanRepresentation) {
        this.booleanRepresentation = booleanRepresentation;
    }

    public boolean getRepresentation()  {
        return booleanRepresentation;
    }

    public static Status getFromBoolean(boolean booleanRepresentation) {
        if (booleanRepresentation) {
            return UP;
        } else {
            return DOWN;
        }
    }
}
