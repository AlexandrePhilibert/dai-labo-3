package ch.heigvd.server;

import ch.heigvd.protocol.StatusDatagram;
import ch.heigvd.protocol.types.Status;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;

public class ServerState {
    private static final int DOWN_TIMEOUT_SECONDS = 30;

    public record ServiceStatus(
            Instant receptionInstant,
            StatusDatagram status
    ) {
    }

    public record Service(
            String name,
            boolean hidden,
            List<ServiceStatus> statuses
    ) {
        public boolean isUp() {
            ServiceStatus last = getLastStatus();

            if (last == null) {
                return false;
            }

            return Instant.now().minus(DOWN_TIMEOUT_SECONDS, ChronoUnit.SECONDS).isBefore(last.receptionInstant())
                    && last.status().get("up").value().equals("true");
        }

        public ServiceStatus getLastStatus() {
            if (statuses.isEmpty()) {
                return null;
            }

            return statuses.get(statuses.size() - 1);
        }

        public StatusDatagram getAverage() {
            if (statuses.isEmpty()) {
                return null;
            }
            // Get the original status, and iterate over the keys:
            StatusDatagram last = statuses.get(statuses.size() - 1).status();
            StatusDatagram result = new StatusDatagram();

            last.getAllKeys().forEach(e -> {
                // Filter if the value is not a number
                Optional<Double> value = tryGetDouble(last.get(e).value());
                if (value.isPresent()) {
                    result.set(e, String.valueOf(statuses.stream()
                            .flatMap(status -> tryGetDouble(status.status().get(e).value()).stream())
                            // Unwraps the double (Double -> double)
                            .mapToDouble(i -> i)
                            .average()
                            // We consider that the total is a constant / the last value for the averages
                            .orElse(0)), last.get(e).total().orElse(null));
                } else {
                    // Just insert the last value
                    result.set(e, last.get(e).value(), last.get(e).total().orElse(null));
                }
            });

            return result;
        }
    }

    // TODO: Find a more optimized way to store this kind of data
    //  Or even better, a database, due to the multiple indexes / filters needed.
    private List<Service> services = new ArrayList<>();

    public Service getService(String name) {
        return services.stream()
                .filter(service -> service.name().equals(name))
                .findFirst()
                .orElse(null);
    }

    public List<Service> getAllServices() {
        return Collections.unmodifiableList(services);
    }

    public void putStatus(StatusDatagram datagram, boolean hidden) {
        // Check for a given service
        String name = datagram.get("name").value();
        Service service = getService(name);

        if (service == null) {
            service = new Service(name, hidden, new ArrayList<>());
            services.add(service);
        }

        service.statuses().add(new ServiceStatus(Instant.now(), datagram));
    }


    private static Optional<Double> tryGetDouble(String input) {
        try {
            return Optional.of(Double.parseDouble(input));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}
