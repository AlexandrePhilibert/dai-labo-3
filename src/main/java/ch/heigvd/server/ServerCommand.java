package ch.heigvd.server;

import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ch.heigvd.protocol.StatusDatagram;
import ch.heigvd.protocol.request.GetRequest;
import ch.heigvd.protocol.request.ListRequest;
import ch.heigvd.protocol.response.ErrorResponse;
import ch.heigvd.protocol.response.ListResponse;
import ch.heigvd.protocol.types.ErrorReason;
import ch.heigvd.protocol.types.GetType;
import ch.heigvd.protocol.types.Status;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine;

@CommandLine.Command(name = "server", mixinStandardHelpOptions = true, version = "0.0.1", description = "A server implementation of the CPT protocol")
public class ServerCommand implements Callable<Integer> {
    private static final Logger LOGGER = LogManager.getLogger(ServerCommand.class);

    @CommandLine.Option(
            names = {"--public-host"},
            description = "The multicast public subnet to use",
            defaultValue = "239.0.192.5"
    )
    private String publicHost;

    @CommandLine.Option(
            names = {"--private-host"},
            description = "The multicast private subnet to use",
            defaultValue = "239.0.192.6"
    )
    private String privateHost;

    @CommandLine.Option(
            names = {"--emitter-port"},
            description = "The emitter port to use",
            defaultValue = "25591"
    )
    private int emitterPort;

    @CommandLine.Option(
            names = {"--receiver-port"},
            description = "The receiver port to use",
            defaultValue = "25592"
    )
    private int receiverPort;

    @CommandLine.Option(
            names = {"-i", "--interface"},
            description = "The interface to use",
            required = true
    )
    private String interfaceName;


    //
    private ServerState state = new ServerState();

    @Override
    public Integer call() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(3);

        CountDownLatch latch = new CountDownLatch(1);

        executor.submit(new EmitterHandler(state, publicHost, emitterPort, interfaceName, false));
        executor.submit(new EmitterHandler(state, privateHost, emitterPort, interfaceName, true));
        executor.submit(new ClientHandler(state, receiverPort));

        latch.await();

        return 0;
    }

    private record EmitterHandler(
            ServerState state,
            String host,
            int port,
            String interfaceName,
            boolean isHidden
    ) implements Runnable {
        @Override
        public void run() {
            try (MulticastSocket receiverSocket = new MulticastSocket(port)) {
                InetAddress multicastAddress = InetAddress.getByName(host);
                InetSocketAddress group = new InetSocketAddress(multicastAddress, port);
                NetworkInterface networkInterface = NetworkInterface.getByName(interfaceName);
                receiverSocket.joinGroup(group, networkInterface);

                byte[] data = new byte[1024];

                while (true) {
                    DatagramPacket datagram = new DatagramPacket(data, data.length);

                    receiverSocket.receive(datagram);

                    String message = new String(
                            datagram.getData(),
                            datagram.getOffset(),
                            datagram.getLength(),
                            StandardCharsets.UTF_8
                    );

                    // Parse the datagram
                    StatusDatagram parsedDatagram = StatusDatagram.parse(message);

                    // If the datagram has a name, use it
                    boolean hasName = parsedDatagram.get("name") != null;
                    if (hasName) {
                        state.putStatus(parsedDatagram, isHidden);
                    }
                }

            } catch (Exception e) {
                LOGGER.error("Error while creating socket", e);
            }
        }
    }

    private record ClientHandler(
            ServerState state,
            int port
    ) implements Runnable {
        @Override
        public void run() {
            try (DatagramSocket socket = new DatagramSocket(port)) {
                byte[] data = new byte[1024];

                while (true) {
                    DatagramPacket datagram = new DatagramPacket(data, data.length);

                    socket.receive(datagram);

                    String message = new String(
                            datagram.getData(),
                            datagram.getOffset(),
                            datagram.getLength(),
                            StandardCharsets.UTF_8
                    ).trim();

                    String response = switch (message.split(" ")[0].toLowerCase(Locale.ROOT)) {
                        case "get" -> {
                            GetRequest request = GetRequest.parse(message);
                            if (request == null) {
                                yield new ErrorResponse(ErrorReason.UNKNOWN_COMMAND).toString();
                            }

                            // Get the service
                            ServerState.Service service = state.getService(request.getServiceName());

                            if (service == null) {
                                yield new ErrorResponse(ErrorReason.UNKNOWN_SERVICE).toString();
                            }

                            GetType type = request.getType();
                            if (type == null) {
                                type = GetType.LAST;
                            }

                            yield (Optional.ofNullable(switch (type) {
                                case AVERAGE -> service.getAverage();
                                case LAST -> service.getLastStatus().status();
                            })).map(Object::toString).orElse("");
                        }
                        case "list" -> {
                            ListRequest request = ListRequest.getRequest(message);
                            if (request == null) {
                                yield new ErrorResponse(ErrorReason.UNKNOWN_COMMAND).toString();
                            }
                            ListResponse listResponse = new ListResponse();
                            state.getAllServices().stream()
                                    .filter(e -> !e.hidden())
                                    .filter(e -> request.getRequestedStatus() == null
                                            || request.getRequestedStatus() == Status.getFromBoolean(e.isUp()))
                                    .forEach(e -> listResponse.add(e.name(), Status.getFromBoolean(e.isUp())));

                            yield listResponse.toString();
                        }
                        default -> new ErrorResponse(ErrorReason.UNKNOWN_COMMAND).toString();
                    };

                    byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
                    DatagramPacket responsePacket = new DatagramPacket(
                            bytes,
                            bytes.length,
                            datagram.getSocketAddress()
                    );

                    socket.send(responsePacket);
                }
            } catch (Exception e) {
                LOGGER.error("Could not create receiver handler socket", e);
            }
        }
    }
}
