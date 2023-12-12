package ch.heigvd.emitter;

import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "emitter", mixinStandardHelpOptions = true, version = "0.0.1", description = "The SMTP emitter")
public class EmitterCommand implements Callable<Integer> {
    @Override
    public Integer call() throws Exception {
        return null;
    }
}
