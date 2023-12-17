package ch.heigvd.protocol.response;

import ch.heigvd.protocol.types.Status;

import java.util.HashMap;
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
}
