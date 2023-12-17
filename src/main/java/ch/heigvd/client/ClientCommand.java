package ch.heigvd.client;

import ch.heigvd.protocol.StatusDatagram;
import ch.heigvd.protocol.response.ListResponse;
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
            names={"-H", "--host"},
            description="The host of the server to connect to.",
            defaultValue = "localhost"
    )
    private String host;

    @CommandLine.Option(
            names={"-n", "--service-name"},
            description="The name of the service to watch"
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
            LOGGER.error("Error while creating socket", e);
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
                System.out.println(ListResponse.parse(message));
            } else {
                System.out.println(StatusDatagram.parse(message));
            }


        } catch (Exception e) {
            LOGGER.error("An exception occurred while polling the latest information", e);
        }
    }

    /**
     * Clears the terminal
     */
    private void clear() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
}
