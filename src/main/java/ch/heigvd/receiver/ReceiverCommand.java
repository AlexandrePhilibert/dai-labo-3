package ch.heigvd.receiver;

import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "receiver", mixinStandardHelpOptions = true, version = "0.0.1", description = "The SMTP receiver")
public class ReceiverCommand implements Callable<Integer> {
    @Override
    public Integer call() throws Exception {
        return null;
    }
}
