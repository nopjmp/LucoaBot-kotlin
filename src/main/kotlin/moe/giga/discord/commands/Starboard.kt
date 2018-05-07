package moe.giga.discord.commands

import moe.giga.discord.annotations.IsCommand
import moe.giga.discord.contexts.MessageContext
import moe.giga.discord.contexts.ServerContext
import moe.giga.discord.permissions.AccessLevel
import moe.giga.discord.util.MiscUtils
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.JDABuilder
import net.dv8tion.jda.core.entities.*
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionAddEvent
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionRemoveAllEvent
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionRemoveEvent
import net.dv8tion.jda.core.hooks.SubscribeEvent
import java.awt.Color
import java.util.concurrent.ConcurrentHashMap
import java.util.stream.Collectors

@IsCommand
class Starboard : Command() {
    override val name = "starboard"
    override val description = "Sets the starboard channel (use none to reset)"
    override val level = AccessLevel.MOD

    private val snowflakeAssocMap = ConcurrentHashMap<Long, StarboardEntry>()

    private inner class StarboardEntry(internal val channelId: Long, internal val messageId: Long)

    override fun init(builder: JDABuilder) {
        builder.addEventListener(this)
    }

    override fun onCommand(MC: MessageContext, args: List<String>) {
        if (args.isNotEmpty()) {
            if (args[0].equals("none", ignoreCase = true)) {
                MC.serverCtx.starChannel = null
            } else {
                val channel = MC.serverCtx.guild.getTextChannelsByName(args[0], true)[0]
                if (channel != null) {
                    MC.serverCtx.starChannel = channel.id
                    MC.serverCtx.save()
                    MC.sendMessage(String.format("Star channel set to %s", channel.asMention)).queue()
                }
            }
        } else {
            MC.sendMessage("args invalid?").queue()
        }
    }

    private fun makeText(message: Message, count: Int): String {
        return String.format("⭐ **%d** %s ID: %d", count, message.textChannel.asMention, message.idLong)
    }

    private fun makeEmbed(message: Message, count: Int): MessageEmbed {
        val scale = 255 - Math.min(Math.max(0, count - DEFAULT_THRESHOLD) * 25, 255)
        val embed = EmbedBuilder()
                .setColor(Color(255, 255, scale))
                .setAuthor(MiscUtils.username(message.author), message.author.avatarUrl)
                .setDescription(message.contentDisplay)
                .setTimestamp(message.creationTime)

        if (!message.attachments.isEmpty()) {
            val attachment = message.attachments[0]
            if (attachment.width > 0 || attachment.height > 0) {
                embed.setImage(attachment.url)
            }
        } else if (!message.embeds.isEmpty()) {
            val messageEmbed = message.embeds[0]
            if (messageEmbed.type == EmbedType.IMAGE) {
                embed.setImage(messageEmbed.thumbnail.url)
            }
        }

        return embed.build()
    }

    private fun onReaction(guild: Guild, messageId: Long, message: Message, messageReactions: List<MessageReaction>) {
        val sc = ServerContext(guild)

        if (sc.starChannel != null && sc.starChannel != message.channel.id) {
            val entry = snowflakeAssocMap[messageId]

            if (messageReactions.size < DEFAULT_THRESHOLD) {
                if (entry != null) {
                    snowflakeAssocMap.remove(messageId)
                    val channel = guild.getTextChannelById(entry.channelId)
                    channel.getMessageById(entry.messageId).queue { it.delete().queue() }
                }
            } else {
                val count = messageReactions.size
                val text = makeText(message, count)
                val embed = makeEmbed(message, count)

                if (entry == null) {
                    val channel = guild.getTextChannelById(sc.starChannel)
                    val newMessage = channel.sendMessage(embed).content(text).complete()
                    snowflakeAssocMap[message.idLong] = StarboardEntry(channel.idLong, newMessage.idLong)
                } else {
                    val channel = guild.getTextChannelById(entry.channelId)
                    val existingMessage = channel.getMessageById(entry.messageId).complete()
                    existingMessage.editMessage(embed).content(text).queue()
                }
            }
        }
    }

    @SubscribeEvent
    fun addReactionEvent(event: GuildMessageReactionAddEvent) {
        val message = event.channel.getMessageById(event.messageIdLong).complete()
        val messageReactions = message.reactions.stream()
                .filter { reaction -> reaction.reactionEmote.name!!.contentEquals("⭐") }
                .collect(Collectors.toList())
        this.onReaction(event.guild, event.messageIdLong, message, messageReactions)
    }

    @SubscribeEvent
    fun removeReactionEvent(event: GuildMessageReactionRemoveEvent) {
        val message = event.channel.getMessageById(event.messageIdLong).complete()
        val messageReactions = message.reactions.stream()
                .filter { reaction -> reaction.reactionEmote.name!!.contentEquals("⭐") }
                .collect(Collectors.toList())
        this.onReaction(event.guild, event.messageIdLong, message, messageReactions)
    }

    @SubscribeEvent
    fun removeAllReactionEvent(event: GuildMessageReactionRemoveAllEvent) {
        val message = event.channel.getMessageById(event.messageIdLong).complete()
        this.onReaction(event.guild, event.messageIdLong, message, emptyList())
    }

    companion object {
        private const val DEFAULT_THRESHOLD = 3
    }
}
