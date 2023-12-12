package ch.heigvd.emitter;

import org.apache.logging.log4j.core.config.Configurator;
import picocli.CommandLine;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;

@CommandLine.Command(name = "emitter", mixinStandardHelpOptions = true, version = "0.0.1", description = "The SMTP emitter")
public class EmitterCommand implements Callable<Integer> {
    private static final Logger LOGGER = LogManager.getLogger(EmitterCommand.class);

    @CommandLine.Option(
            names = {"-H", "--host"},
            description = "The multicast subnet to use",
            required = true
    )
    private String host;

    @CommandLine.Option(
            names = {"-p", "--port"},
            description = "The port to use",
            defaultValue = "25591"
    )
    private int port;

    @CommandLine.Option(
            names = {"-i", "--interface"},
            description = "The network interface to use",
            required = true
    )
    private String interfaceName;

    @CommandLine.Option(
            names = {"-d", "--delay"},
            description = "The delay before sending a new request (in ms)",
            defaultValue = "0"
    )
    private int delay;

    @CommandLine.Option(
            names = {"-f", "--frequency"},
            description = "The frequency between requests (in ms)",
            defaultValue = "5000"
    )
    private int frequency;

    @Override
    public Integer call() {
        Configurator.setRootLevel(Level.INFO);

        CountDownLatch latch = new CountDownLatch(1);

        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

        try (MulticastSocket socket = new MulticastSocket(port)) {
            InetAddress multicastAddress = InetAddress.getByName(host);
            InetSocketAddress group = new InetSocketAddress(multicastAddress, port);
            NetworkInterface networkInterface = NetworkInterface.getByName(interfaceName);
            socket.joinGroup(group, networkInterface);

            executor.scheduleAtFixedRate(() -> send(socket, group), delay, frequency, TimeUnit.MILLISECONDS);

            // We have to await the latch inside the try-with-resource, otherwise the socket will be closed
            latch.await();
        } catch (Exception e) {
            LOGGER.error("Could not create multicast socket");
            return 1;
        }

        executor.shutdown();

        return 0;
    }

    private static void send(MulticastSocket socket, InetSocketAddress group) throws RuntimeException {
        String message = "hello";

        byte[] payload = message.getBytes(StandardCharsets.UTF_8);

        DatagramPacket datagram = new DatagramPacket(
                payload,
                payload.length,
                group
        );

        try {
            socket.send(datagram);
        } catch (IOException e) {
            LOGGER.error("Could not send datagram: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
