package ch.heigvd.protocol.response;

import ch.heigvd.protocol.types.ErrorReason;

public class ErrorResponse {
    private final ErrorReason reason;

    public ErrorResponse(ErrorReason reason) {
        this.reason = reason;
    }

    @Override
    public String toString() {
        return "ERR " + reason.getValue();
    }
}
