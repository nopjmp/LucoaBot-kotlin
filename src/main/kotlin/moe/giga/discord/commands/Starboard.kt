package moe.giga.discord.commands

import moe.giga.discord.annotations.IsCommand
import moe.giga.discord.contexts.MessageContext
import moe.giga.discord.contexts.ServerContext
import moe.giga.discord.util.AccessLevel
import moe.giga.discord.util.username
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.JDABuilder
import net.dv8tion.jda.core.entities.EmbedType
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.MessageEmbed
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionAddEvent
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionRemoveAllEvent
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionRemoveEvent
import net.dv8tion.jda.core.hooks.SubscribeEvent
import java.awt.Color
import java.time.OffsetDateTime
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.timer

@IsCommand
class Starboard : Command() {
    override val name = "starboard"
    override val description = "Sets the starboard channel (use none to reset)"
    override val level = AccessLevel.MOD

    private val argRegex = Regex("""<#(\d+)>""")

    private val snowflakeAssocMap = ConcurrentHashMap<Long, StarboardEntry>()

    private inner class StarboardEntry(internal val channelId: Long, internal val messageId: Long, internal val time: OffsetDateTime)

    override fun init(builder: JDABuilder) {
        builder.addEventListener(this)

        timer("SnowflakeAssocClean", period = 30000, action = {
            snowflakeAssocMap.filter {
                it.value.time.isBefore(OffsetDateTime.now().minusDays(1))
            }.forEach { snowflakeAssocMap.remove(it.key) }
        })
    }

    override fun onCommand(MC: MessageContext, args: List<String>) {
        if (args.isNotEmpty()) {
            if (args[0].equals("none", ignoreCase = true)) {
                MC.serverCtx.starChannel = null
            } else if (argRegex.matches(args[0])) {
                val matches = argRegex.find(args[0])
                val channelId = matches?.groups?.get(1)?.value
                val channel = MC.serverCtx.guild.getTextChannelById(channelId)
                if (channel != null) {
                    MC.serverCtx.starChannel = channel.id
                    MC.serverCtx.save()
                    MC.sendMessage("Star channel set to ${channel.asMention}").queue()
                    return
                }
            }
        }
        MC.sendError("args invalid?").queue()
    }

    private fun makeText(message: Message, count: Int): String {
        return "⭐ **$count** ${message.textChannel.asMention} ID: ${message.idLong}"
    }

    private fun makeEmbed(message: Message, count: Int): MessageEmbed {
        val scale = 255 - Math.min(Math.max(0, count - DEFAULT_THRESHOLD) * 25, 255)
        val author = message.jda.getUserById(message.author.id)
        val embed = EmbedBuilder()
                .setColor(Color(255, 255, scale))
                .setAuthor(author.username(), null, author.effectiveAvatarUrl)
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

    private fun onReaction(guild: Guild, messageId: Long, message: Message, count: Int) {
        val sc = ServerContext(guild)

        if (sc.starChannel != null && sc.starChannel != message.channel.id) {
            val entry = snowflakeAssocMap[messageId]

            if (count < DEFAULT_THRESHOLD) {
                if (entry != null) {
                    snowflakeAssocMap.remove(messageId)
                    val channel = guild.getTextChannelById(entry.channelId)
                    channel.getMessageById(entry.messageId).queue { it.delete().queue() }
                }
            } else {
                val text = makeText(message, count)
                val embed = makeEmbed(message, count)

                if (entry == null) {
                    val channel = guild.getTextChannelById(sc.starChannel)
                    val newMessage = channel.sendMessage(embed).content(text).complete()
                    snowflakeAssocMap[message.idLong] = StarboardEntry(channel.idLong, newMessage.idLong, newMessage.creationTime)
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
        if (message.creationTime.isAfter(OffsetDateTime.now().minusDays(1))) {
            val starReaction = message.reactions.find { it.reactionEmote.name == "⭐" }
            if (starReaction != null)
                this.onReaction(event.guild, event.messageIdLong, message, starReaction.count)
        }
    }

    @SubscribeEvent
    fun removeReactionEvent(event: GuildMessageReactionRemoveEvent) {
        val message = event.channel.getMessageById(event.messageIdLong).complete()
        if (message.creationTime.isAfter(OffsetDateTime.now().minusDays(1))) {
            val starReaction = message.reactions.find { it.reactionEmote.name == "⭐" }
            if (starReaction != null)
                this.onReaction(event.guild, event.messageIdLong, message, starReaction.count)
        }
    }

    @SubscribeEvent
    fun removeAllReactionEvent(event: GuildMessageReactionRemoveAllEvent) {
        val message = event.channel.getMessageById(event.messageIdLong).complete()
        if (message.creationTime.isAfter(OffsetDateTime.now().minusDays(1))) {
            this.onReaction(event.guild, event.messageIdLong, message, 0)
        }
    }

    companion object {
        private const val DEFAULT_THRESHOLD = 3
    }
}
