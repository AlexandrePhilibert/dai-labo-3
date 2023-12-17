package ch.heigvd.protocol.request;

import ch.heigvd.protocol.types.Status;

import java.util.Locale;

public class ListRequest {
    private Status requestedStatus;


    public Status getRequestedStatus() {
        return requestedStatus;
    }

    public static ListRequest getRequest(String datagram) {
        String lowerDatagram = datagram.toLowerCase(Locale.ROOT);
        if (!lowerDatagram.startsWith("list")) {
            return null;
        }
        // generate a parser for the following format
        // LIST [type=(up|down)]
        String[] parts = lowerDatagram.split(" ");
        ListRequest listRequest = new ListRequest();
        if (parts.length > 1) {
            String[] statusParts = parts[1].split("=");
            if (statusParts.length > 1) {
                boolean booleanRepresentation = Boolean.parseBoolean(statusParts[1]);
                listRequest.requestedStatus = Status.getFromBoolean(booleanRepresentation);
            }
        }

        return listRequest;
    }
}
