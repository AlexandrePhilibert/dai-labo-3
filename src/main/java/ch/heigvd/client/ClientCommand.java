package ch.heigvd.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "client", mixinStandardHelpOptions = true, version = "0.0.1", description = "The SMTP receiver")
public class ClientCommand implements Callable<Integer> {
    private static final Logger LOGGER = LogManager.getLogger(ClientCommand.class);

    @CommandLine.Option(
            names = {"-p", "--port"},
            description = "The port of the server to connect to.",
            defaultValue = "25591"
    )
    private int port;

    @CommandLine.Option(
            names={"-H", "--host"},
            description="The host of the server to connect to.",
            defaultValue = "localhost"
    )
    private String host;

    @Override
    public Integer call() throws Exception {
        try (DatagramSocket socket = new DatagramSocket()) {
            byte[] buffer = new byte[1024];
            DatagramPacket datagram = new DatagramPacket(buffer, buffer.length);
            while (true) {
                String ping = "PING";

                DatagramPacket pingPacket = new DatagramPacket(
                        ping.getBytes(StandardCharsets.UTF_8),
                        ping.getBytes(StandardCharsets.UTF_8).length,
                        new InetSocketAddress(InetAddress.getByName(host), port)
                );

                socket.send(pingPacket);

                System.out.println("Sent packet!");

                socket.receive(datagram);

                String message = new String(
                        datagram.getData(),
                        datagram.getOffset(),
                        datagram.getLength(),
                        StandardCharsets.UTF_8
                );

                System.out.println(message);
            }

        } catch (Exception e) {
            LOGGER.error("Error while creating socket", e);
            return 1;
        }
    }
}
