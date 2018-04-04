package moe.giga.discord.contexts;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public final class MessageContext {
    private UserContext UC;
    private ServerContext SC;

    private MessageReceivedEvent event;

    public MessageContext(MessageReceivedEvent event) {
        this.event = event;
    }

    public JDA getJDA() {
        return event.getJDA();
    }

    public UserContext getUC() {
        return UC;
    }

    public void setUC(UserContext UC) {
        this.UC = UC;
    }

    public ServerContext getSC() {
        return SC;
    }

    public void setSC(ServerContext SC) {
        this.SC = SC;
    }

    public MessageReceivedEvent getEvent() {
        return event;
    }

    private Message sendMessage(Message message) {
        if (event.isFromType(ChannelType.PRIVATE))
            return event.getPrivateChannel().sendMessage(message).complete();
        else
            return event.getTextChannel().sendMessage(message).complete();
    }

    public Message sendMessage(String message) {
        return sendMessage(new MessageBuilder().append(message).build());
    }

    public void sendMessage(MessageEmbed message) {
        if (event.isFromType(ChannelType.PRIVATE)) {
            event.getPrivateChannel().sendMessage(message).complete();
        } else {
            event.getTextChannel().sendMessage(message).complete();
        }
    }
}
