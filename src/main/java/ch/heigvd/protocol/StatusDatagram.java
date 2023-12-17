package ch.heigvd.protocol;

import java.util.*;
import java.util.stream.Collectors;

public class StatusDatagram {
    public record Entry(String metric,
                        String value,
                        Optional<String> total) {
        public static Entry get(String metric, String value) {
            return new Entry(metric, value, Optional.empty());
        }

        public static Entry get(String metric, String value, String total) {
            return new Entry(metric, value, Optional.ofNullable(total));
        }
    }

    private final LinkedHashMap<String, Entry> values = new LinkedHashMap<>();

    public void set(String metric, String value) {
        values.put(metric, Entry.get(metric, value));
    }

    public void set(String metric, String value, String total) {
        values.put(metric, Entry.get(metric, value, total));
    }

    public Set<String> getAllKeys() {
        return values.keySet();
    }

    public Entry get(String metric) {
        return values.get(metric);
    }


    public static StatusDatagram parse(String packet) {
        StatusDatagram datagram = new StatusDatagram();
        for (String line : packet.split("\n")) {
            // Split by the first enter
            String[] separated = line.split("=", 2);
            if (separated.length == 1) {
                // We continue, if the packet is malformed, we still want to scrap a bit of the data.
                continue;
            }

            String prefix = separated[0];
            String suffix = separated[1];

            String[] suffixSplit = suffix.split("/", 2);

            if (suffixSplit.length == 1) {
                datagram.set(prefix, suffixSplit[0]);
            } else {
                datagram.set(prefix, suffixSplit[0], suffixSplit[1]);
            }
        }

        return datagram;
    }

    @Override
    public String toString() {
        return values.values().stream().map(entry -> {
            String start = entry.metric() + "=" + entry.value();
            if (entry.total().isPresent()) {
                return start + "/" + entry.total().get();
            } else {
                return start;
            }
        }).collect(Collectors.joining("\n"));
    }
}
