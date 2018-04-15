package moe.giga.discord.commands;

import moe.giga.discord.annotations.CommandInfo;
import moe.giga.discord.contexts.MessageContext;
import moe.giga.discord.contexts.ServerContext;
import moe.giga.discord.util.MiscUtils;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionRemoveAllEvent;
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionRemoveEvent;
import net.dv8tion.jda.core.hooks.SubscribeEvent;

import java.awt.*;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@CommandInfo(name = "starboard", description = "Sets the starboard channel (use none to reset)")
public final class Starboard extends Command {

    private static final int DEFAULT_THRESHOLD = 3;

    private class StarboardEntry {
        private final long channelId;
        private final long messageId;

        private StarboardEntry(long channelId, long messageId) {
            this.channelId = channelId;
            this.messageId = messageId;
        }

        long getChannelId() {
            return channelId;
        }

        long getMessageId() {
            return messageId;
        }
    }

    private ConcurrentHashMap<Long, StarboardEntry> snowflakeAssocMap = new ConcurrentHashMap<>();

    @Override
    public void init(JDABuilder builder) {
        builder.addEventListener(this);
    }

    @Override
    public void onCommand(MessageContext MC, String[] args) {
        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("none")) {
                MC.getSC().setStarChannel(null);
            } else {
                TextChannel channel = MC.getEvent().getGuild().getTextChannelsByName(args[0], true).get(0);
                if (channel != null) {
                    MC.getSC().setStarChannel(channel.getId());
                    MC.sendMessage(String.format("Star channel set to %s", channel.getAsMention()));
                }
            }
        } else {
            MC.sendMessage("args invalid?");
        }
    }

    private String makeText(Message message, int count) {
        return String.format("⭐ **%d** %s ID: %d", count, message.getTextChannel().getAsMention(), message.getIdLong());
    }

    private MessageEmbed makeEmbed(Message message, int count) {
        final int scale = 255 - Math.min(Math.max(0, count - DEFAULT_THRESHOLD) * 25, 255);
        EmbedBuilder embed = new EmbedBuilder()
                .setColor(new Color(255, 255, scale))
                .setAuthor(MiscUtils.username(message.getAuthor()), message.getAuthor().getAvatarUrl())
                .setDescription(message.getContentDisplay())
                .setTimestamp(message.getCreationTime());

        if (!message.getAttachments().isEmpty()) {
            Message.Attachment attachment = message.getAttachments().get(0);
            if (attachment.getWidth() > 0 || attachment.getHeight() > 0) {
                embed.setImage(attachment.getUrl());
            }
        } else if (!message.getEmbeds().isEmpty()) {
            MessageEmbed messageEmbed = message.getEmbeds().get(0);
            if (messageEmbed.getType() == EmbedType.IMAGE) {
                embed.setImage(messageEmbed.getThumbnail().getUrl());
            }
        }

        return embed.build();
    }

    private void onReaction(Guild guild, long messageId, Message message, List<MessageReaction> messageReactions) {
        ServerContext SC = new ServerContext(guild);

        if (SC.getStarChannel() != null && !SC.getStarChannel().equals(message.getChannel().getId())) {
            StarboardEntry entry = snowflakeAssocMap.get(messageId);

            if (messageReactions.isEmpty()) {
                if (entry != null) {
                    snowflakeAssocMap.remove(messageId);
                    TextChannel channel = guild.getTextChannelById(entry.getChannelId());
                    channel.getMessageById(entry.getMessageId()).queue(msg -> msg.delete().queue());
                }
            } else {
                int count = messageReactions.size();
                String text = makeText(message, count);
                MessageEmbed embed = makeEmbed(message, count);

                if (entry == null) {
                    TextChannel channel = guild.getTextChannelById(SC.getStarChannel());
                    Message newMessage = channel.sendMessage(embed).content(text).complete();
                    snowflakeAssocMap.put(message.getIdLong(), new StarboardEntry(channel.getIdLong(), newMessage.getIdLong()));
                } else {
                    TextChannel channel = guild.getTextChannelById(entry.getChannelId());
                    Message existingMessage = channel.getMessageById(entry.getMessageId()).complete();
                    existingMessage.editMessage(embed).content(text).queue();
                }
            }
        }
    }

    @SubscribeEvent
    public void addReactionEvent(GuildMessageReactionAddEvent event) {
        Message message = event.getChannel().getMessageById(event.getMessageIdLong()).complete();
        List<MessageReaction> messageReactions = message.getReactions().stream()
                .filter(reaction -> reaction.getReactionEmote().getName().contentEquals("⭐"))
                .collect(Collectors.toList());
        this.onReaction(event.getGuild(), event.getMessageIdLong(), message, messageReactions);
    }

    @SubscribeEvent
    public void removeReactionEvent(GuildMessageReactionRemoveEvent event) {
        Message message = event.getChannel().getMessageById(event.getMessageIdLong()).complete();
        List<MessageReaction> messageReactions = message.getReactions().stream()
                .filter(reaction -> reaction.getReactionEmote().getName().contentEquals("⭐"))
                .collect(Collectors.toList());
        this.onReaction(event.getGuild(), event.getMessageIdLong(), message, messageReactions);
    }

    @SubscribeEvent
    public void removeAllReactionEvent(GuildMessageReactionRemoveAllEvent event) {
        Message message = event.getChannel().getMessageById(event.getMessageIdLong()).complete();
        this.onReaction(event.getGuild(), event.getMessageIdLong(), message, Collections.emptyList());
    }
}
