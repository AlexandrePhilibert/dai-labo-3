package ch.heigvd.client;

import ch.heigvd.protocol.StatusDatagram;
import ch.heigvd.protocol.response.ListResponse;
import ch.heigvd.protocol.types.Status;
import com.diogonunes.jcolor.Attribute;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.diogonunes.jcolor.Ansi.RESET;
import static com.diogonunes.jcolor.Ansi.colorize;

import java.util.Map;
import java.util.concurrent.*;

@CommandLine.Command(name = "client", mixinStandardHelpOptions = true, version = "0.0.1", description = "The SMTP receiver")
public class ClientCommand implements Callable<Integer> {
    private static final Logger LOGGER = LogManager.getLogger(ClientCommand.class);

    @CommandLine.Option(
            names = {"-p", "--port"},
            description = "The port of the server to connect to.",
            defaultValue = "25592"
    )
    private int port;

    @CommandLine.Option(
            names = {"-H", "--host"},
            description = "The host of the server to connect to.",
            defaultValue = "localhost"
    )
    private String host;

    @CommandLine.Option(
            names = {"-n", "--service-name"},
            description = "The name of the service to watch"
    )
    private String serviceName;

    @Override
    public Integer call() throws Exception {
        clear();

        CountDownLatch latch = new CountDownLatch(1);

        try (DatagramSocket socket = new DatagramSocket()) {
            ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

            poll(socket);

            executor.schedule(() -> poll(socket), 5, TimeUnit.SECONDS);

            latch.await();
        } catch (Exception e) {
            LOGGER.error("Error while creating socket: {}", e.getLocalizedMessage());
            return 1;
        }

        return 0;
    }

    private void poll(DatagramSocket socket) {
        try {
            byte[] buffer = new byte[1024];
            DatagramPacket datagram = new DatagramPacket(buffer, buffer.length);

            String command = serviceName == null ? "LIST" : "GET " + serviceName;

            DatagramPacket pingPacket = new DatagramPacket(
                    command.getBytes(StandardCharsets.UTF_8),
                    command.getBytes(StandardCharsets.UTF_8).length,
                    new InetSocketAddress(InetAddress.getByName(host), port)
            );

            socket.send(pingPacket);

            socket.receive(datagram);

            String message = new String(
                    datagram.getData(),
                    datagram.getOffset(),
                    datagram.getLength(),
                    StandardCharsets.UTF_8
            );

            clear();

            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Date date = new Date();
            System.out.println("Last updated at: " + dateFormat.format(date));
            System.out.println("---");

            if (serviceName == null) {
                print(ListResponse.parse(message));
            } else {
                print(StatusDatagram.parse(message));
            }


        } catch (Exception e) {
            LOGGER.error("An exception occurred while polling the latest information: {}", e.getLocalizedMessage());
        }
    }

    private void print(StatusDatagram parse) {
        parse.getAllKeys().forEach(key -> {
            StatusDatagram.Entry entry = parse.get(key);
            String total = "";

            if (entry.total().isPresent()) {
               total = colorize(" / ", RESET) + colorize(entry.total().get(), Attribute.BRIGHT_WHITE_TEXT());
            }

            System.out.println(
                    colorize(entry.metric() + "=", Attribute.WHITE_TEXT()) +
                            colorize(entry.value(), Attribute.BRIGHT_WHITE_TEXT()) +
                            total
            );
        });
    }

    void print(ListResponse response) {
        Map<String, Status> statusMap = response.statusMap();
        int longestKey = statusMap.keySet().stream()
                .mapToInt(String::length)
                .max()
                .orElse(0) + 4;

        statusMap.forEach((key, value) -> {
            String status = switch (value) {
                case UP -> colorize(value.name(), Attribute.BRIGHT_GREEN_TEXT());
                case DOWN -> colorize(value.name(), Attribute.BRIGHT_RED_TEXT());
            };
            System.out.println(String.format("%-" + longestKey + "s", key) + status);
        });
    }

    /**
     * Clears the terminal
     */
    private void clear() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
}
