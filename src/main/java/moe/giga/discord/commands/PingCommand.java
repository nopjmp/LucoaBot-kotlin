package moe.giga.discord.commands;

import moe.giga.discord.annotations.CommandInfo;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.time.Duration;
import java.time.Instant;

@CommandInfo(name = "ping", description = "Ping command")
public class PingCommand extends Command {

    @Override
    public void onCommand(MessageReceivedEvent e, String[] args) {
        Instant start = Instant.now();
        Message m = sendMessage(e, "Pong!");
        Instant end = Instant.now();
        long delta = Duration.between(start, end).toMillis();

        m.editMessage(String.format("Pong! %dms", delta)).complete();
    }
}
