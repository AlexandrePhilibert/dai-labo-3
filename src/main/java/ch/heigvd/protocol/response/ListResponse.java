package ch.heigvd.protocol.response;

import ch.heigvd.protocol.types.Status;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ListResponse {
    private final Map<String, Status> statusMap = new HashMap<>();

    public void add(String serviceName, Status status){
        statusMap.put(serviceName, status);
    }


    @Override
    public java.lang.String toString() {
        StringBuilder builder = new StringBuilder();
        statusMap.forEach((key, value) ->
                builder.append(key).append(" ").append(value.name()).append("\n")
        );

        return builder.toString();
    }

    public static ListResponse parse(String input) {
        String[] lines = input.split("\n");
        ListResponse response = new ListResponse();
        for (String line : lines) {
            String[] parts = line.split(" ");
            String serviceName = parts[0];
            Status status = Status.valueOf(parts[1]);
            response.add(serviceName, status);
        }
        return response;
    }
}
