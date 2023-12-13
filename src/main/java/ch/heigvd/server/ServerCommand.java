package ch.heigvd.server;

import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine;

@CommandLine.Command(name = "server", mixinStandardHelpOptions = true, version = "0.0.1", description = "A server implementation of the CPT protocol")
public class ServerCommand implements Callable<Integer> {
    private static final Logger LOGGER = LogManager.getLogger(ServerCommand.class);

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
            description = "The interface to use",
            required = true
    )
    private String interfaceName;

    @Override
    public Integer call() throws Exception {
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
            return 1;
        }
    }
}
