package ch.heigvd.protocol.response;

import ch.heigvd.protocol.types.ErrorReason;

import java.util.Locale;

public class ErrorResponse {
    private final ErrorReason reason;

    public ErrorResponse(ErrorReason reason) {
        this.reason = reason;
    }

    @Override
    public String toString() {
        return "ERR " + reason.getValue();
    }

    public static ErrorResponse parse(String string) {
        // Do the contrary of toString
        String inputLowerCase = string.toLowerCase(Locale.ROOT);
        if (!inputLowerCase.startsWith("err")) {
            return null;
        }

        String reason = inputLowerCase.substring(4);
        ErrorReason parseReason = ErrorReason.fromValue(reason);

        return new ErrorResponse(parseReason);
    }
}
