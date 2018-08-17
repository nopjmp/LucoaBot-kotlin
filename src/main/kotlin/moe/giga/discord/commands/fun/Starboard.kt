package moe.giga.discord.commands.`fun`

import moe.giga.discord.commands.Command
import moe.giga.discord.contexts.MessageContext
import moe.giga.discord.contexts.ServerContext
import moe.giga.discord.util.AccessLevel
import moe.giga.discord.util.username
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.JDABuilder
import net.dv8tion.jda.core.entities.*
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionAddEvent
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionRemoveAllEvent
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionRemoveEvent
import net.dv8tion.jda.core.hooks.SubscribeEvent
import java.awt.Color
import java.time.OffsetDateTime

@Suppress("unused")
class Starboard : Command {
    override val name = "starboard"
    override val description = "Sets the starboard channel (use none to reset)"
    override val level = AccessLevel.MOD

    private val argRegex = Regex("""<#(\d+)>""")

    override fun init(builder: JDABuilder) {
        builder.addEventListener(this)
    }

    override fun execute(MC: MessageContext, args: List<String>) {
        if (MC.server == null)
            throw IllegalArgumentException("You can only use this command on a server.")
        if (args.isNotEmpty()) {
            if (args[0].equals("none", ignoreCase = true)) {
                MC.server.starChannel = null
            } else if (argRegex.matches(args[0])) {
                val matches = argRegex.find(args[0])
                val channelId = matches?.groups?.get(1)?.value
                val channel = MC.server.guild.getTextChannelById(channelId)
                if (channel != null) {
                    MC.server.starChannel = channel.idLong
                    MC.sendMessage("Star channel set to ${channel.asMention}").queue()
                    return
                }
            }
        }
        MC.sendError("args invalid?").queue()
    }

    private fun findStarPost(jda: JDA, channel: TextChannel, message: Message) =
            channel.iterableHistory
                    .limit(100) // 50 seems like a good limit
                    .filter { it.author == jda.selfUser }
                    .filter { it.creationTime.isAfter(OffsetDateTime.now().minusDays(1)) }
                    .find { it.contentRaw.contains("${message.textChannel.asMention} ID: ${message.id}") }
                    ?.let { it.idLong }

    private fun makeText(message: Message, count: Int): String {
        return "$EMOTE **$count** ${message.textChannel.asMention} ID: ${message.idLong}"
    }

    private fun makeEmbed(message: Message, count: Int): MessageEmbed {
        val scale = 255 - Math.min(Math.max(0, count - DEFAULT_THRESHOLD) * 25, 255)
        val author = message.jda.getUserById(message.author.id)
        val embed = EmbedBuilder()
                .setColor(Color(255, 255, scale))
                .setAuthor(author.username(),
                        "https://discordapp.com/channels/${message.guild.id}/${message.channel.id}/${message.id}",
                        author.effectiveAvatarUrl)
                .setDescription(message.contentRaw)
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

    private fun onReaction(guild: Guild, message: Message, count: Int) {
        val sc = ServerContext(guild)

        if (sc.starChannel != null && sc.starChannel != message.channel.idLong) {
            val starChannel = guild.getTextChannelById(sc.starChannel as Long)

            val entryId = findStarPost(guild.jda, starChannel, message)

            if (count < DEFAULT_THRESHOLD) {
                if (entryId != null) {
                    starChannel.getMessageById(entryId).queue { it.delete().queue() }
                }
            } else {
                val text = makeText(message, count)
                val embed = makeEmbed(message, count)

                if (entryId == null) {
                    starChannel.sendMessage(embed).content(text).queue()
                } else {
                    starChannel.getMessageById(entryId).queue({ it.editMessage(embed).content(text).queue() })
                }
            }
        }
    }

    @SubscribeEvent
    fun addReactionEvent(event: GuildMessageReactionAddEvent) {
        val message = event.channel.getMessageById(event.messageIdLong).complete()
        if (event.guild != null && event.reactionEmote.name == EMOTE && message.creationTime.isAfter(OffsetDateTime.now().minusDays(1))) {
            val starReaction = message.reactions.find { it.reactionEmote.name == EMOTE }
            if (starReaction != null)
                this.onReaction(event.guild, message, starReaction.count)
        }
    }

    @SubscribeEvent
    fun removeReactionEvent(event: GuildMessageReactionRemoveEvent) {
        val message = event.channel.getMessageById(event.messageIdLong).complete()
        if (event.guild != null && event.reactionEmote.name == EMOTE && message.creationTime.isAfter(OffsetDateTime.now().minusDays(1))) {
            val starReaction = message.reactions.find { it.reactionEmote.name == EMOTE }
            this.onReaction(event.guild, message, starReaction?.count ?: 0)
        }
    }

    @SubscribeEvent
    fun removeAllReactionEvent(event: GuildMessageReactionRemoveAllEvent) {
        val message = event.channel.getMessageById(event.messageIdLong).complete()
        if (event.guild != null && message.creationTime.isAfter(OffsetDateTime.now().minusDays(1))) {
            this.onReaction(event.guild, message, 0)
        }
    }

    companion object {
        private const val DEFAULT_THRESHOLD = 3
        private const val EMOTE = "⭐"
    }
}
