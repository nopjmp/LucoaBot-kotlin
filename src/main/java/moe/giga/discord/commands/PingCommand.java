package moe.giga.discord.commands;

import moe.giga.discord.annotations.CommandInfo;
import moe.giga.discord.contexts.MessageContext;
import net.dv8tion.jda.core.entities.Message;

import java.time.Duration;
import java.time.Instant;

@CommandInfo(name = "ping", description = "Ping command")
public final class PingCommand extends Command {

    @Override
    public void onCommand(MessageContext MC, String[] args) {
        Instant start = Instant.now();
        Message m = MC.sendMessage("Pong!");
        Instant end = Instant.now();
        long delta = Duration.between(start, end).toMillis();

        m.editMessage(String.format("Pong! %dms", delta)).complete();
    }
}
