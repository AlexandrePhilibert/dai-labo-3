package ch.heigvd;

import ch.heigvd.emitter.EmitterCommand;
import ch.heigvd.receiver.ReceiverCommand;
import ch.heigvd.server.ServerCommand;
import picocli.CommandLine;

@CommandLine.Command(
        name = "smtp",
        mixinStandardHelpOptions = true,
        version = "0.0.1",
        description = "Simple monitoring tool",
        subcommands = {
                ServerCommand.class,
                EmitterCommand.class,
                ReceiverCommand.class,
        }
)
public class ChatCli {
}
