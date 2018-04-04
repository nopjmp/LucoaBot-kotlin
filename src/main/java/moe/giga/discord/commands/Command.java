package moe.giga.discord.commands;

import moe.giga.discord.contexts.MessageContext;
import net.dv8tion.jda.core.JDABuilder;

public abstract class Command {
    public abstract void onCommand(MessageContext MC, String[] args);

    public void init(JDABuilder builder) {

    }
}
