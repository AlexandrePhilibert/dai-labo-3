package ch.heigvd.server;

import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

    @Override
    public Integer call() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(3);

        CountDownLatch latch = new CountDownLatch(1);

        executor.submit(new EmitterHandler(publicHost, emitterPort, interfaceName));
        executor.submit(new EmitterHandler(privateHost, emitterPort, interfaceName));
        executor.submit(new ClientHandler(receiverPort));

        latch.await();

        return 0;
    }

    private record EmitterHandler(
            String host,
            int port,
            String interfaceName
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

                    System.out.println(message);
                }

            } catch (Exception e) {
                LOGGER.error("Error while creating socket");
            }
        }
    }

    private record ClientHandler(
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
                    );

                    System.out.println(message);

                    // If the message is ping, respond with pong
                    if (message.equals("ping")) {
                        byte[] bytes = "pong".getBytes(StandardCharsets.UTF_8);
                        DatagramPacket datagramPacket = new DatagramPacket(
                                bytes,
                                bytes.length,
                                datagram.getSocketAddress()
                        );
                        socket.send(datagramPacket);
                    }
                }
            } catch (Exception e) {
                LOGGER.error("Could not create receiver handler socket");
            }
        }
    }
}
