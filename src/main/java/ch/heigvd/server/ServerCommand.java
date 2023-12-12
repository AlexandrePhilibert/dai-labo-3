package ch.heigvd.server;

import java.util.concurrent.Callable;
import picocli.CommandLine;

@CommandLine.Command(name = "server", mixinStandardHelpOptions = true, version = "0.0.1", description = "A server implementation of the CPT protocol")
public class ServerCommand implements Callable<Integer> {
    @Override
    public Integer call() throws Exception {
        return null;
    }
}
