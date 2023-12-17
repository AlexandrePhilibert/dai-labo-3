package ch.heigvd.protocol.request;

import ch.heigvd.protocol.types.GetType;

import java.util.Locale;

public class GetRequest {
    private final String serviceName;
    private final GetType type;

    public GetRequest(String serviceName, GetType type) {
        this.serviceName = serviceName;
        this.type = type;
    }

    public String getServiceName() {
        return serviceName;
    }

    public GetType getType() {
        return type;
    }

    public static GetRequest parse(String input) {
        String inputLowerCase = input.toLowerCase(Locale.ROOT);
        if (!inputLowerCase.startsWith("get")) {
            return null;
        }

        String[] parts = inputLowerCase.split(" ");
        if (parts.length < 2) {
            throw new IllegalArgumentException("Invalid input format");
        }

        String serviceName = parts[1];
        GetType type = null;

        for (int i = 2; i < parts.length; i++) {
            if (parts[i].startsWith("type=")) {
                String typeValue = parts[i].substring(5);
                type = GetType.get(typeValue);
                break;
            }
        }
        return new GetRequest(serviceName, type);
    }
}
