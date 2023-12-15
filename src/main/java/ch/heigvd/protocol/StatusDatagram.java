package ch.heigvd.protocol;

import javax.swing.text.html.Option;
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
            return new Entry(metric, value, Optional.of(total));
        }
    }

    private final LinkedHashMap<String, Entry> values = new LinkedHashMap<>();

    public void set(String metric, String value) {
        values.put(metric, Entry.get(metric, value));
    }

    public void set(String metric, String value, String total) {
        values.put(metric, Entry.get(metric, value, total));
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
