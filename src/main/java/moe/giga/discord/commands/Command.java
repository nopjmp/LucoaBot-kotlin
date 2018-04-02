package moe.giga.discord.commands;

import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public abstract class Command {
    public abstract void onCommand(MessageReceivedEvent e, String[] args);

    protected Message sendMessage(MessageReceivedEvent e, Message message) {
        if (e.isFromType(ChannelType.PRIVATE))
            return e.getPrivateChannel().sendMessage(message).complete();
        else
            return e.getTextChannel().sendMessage(message).complete();
    }

    protected Message sendMessage(MessageReceivedEvent e, String message) {
        return sendMessage(e, new MessageBuilder().append(message).build());
    }
}
