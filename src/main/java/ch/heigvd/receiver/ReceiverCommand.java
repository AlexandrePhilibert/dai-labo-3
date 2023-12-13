package ch.heigvd.receiver;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "receiver", mixinStandardHelpOptions = true, version = "0.0.1", description = "The SMTP receiver")
public class ReceiverCommand implements Callable<Integer> {
    private static final Logger LOGGER = LogManager.getLogger(ReceiverCommand.class);

    @CommandLine.Option(
            names = {"-p", "--port"},
            description = "The port to use",
            defaultValue = "25591"
    )
    private int port;

    @Override
    public Integer call() throws Exception {
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
            }

        } catch (Exception e) {
            LOGGER.error("Error while creating socket");
            return 1;
        }
    }
}
