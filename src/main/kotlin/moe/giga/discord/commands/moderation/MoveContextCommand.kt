package moe.giga.discord.commands.moderation

import moe.giga.discord.commands.Command
import moe.giga.discord.contexts.MessageContext
import moe.giga.discord.util.AccessLevel
import moe.giga.discord.util.username
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.*

@Suppress("unused")
class MoveContextCommand : Command {
    override val name = "movectx"
    override val aliases = arrayOf("move", "ctxmove")
    override val description = "Removes the last X number of messages."
    override val usage = "prune <number of messages>"
    override val level = AccessLevel.MOD

    private val channelRegex = Regex("""<#(\d+)>""")

    fun transform(message: Message, u: User): MessageEmbed {
        val builder = EmbedBuilder()
                .setDescription(message.contentRaw)
                .setTimestamp(message.creationTime)
                .setFooter("moved by ${u.username()}", "")
                .setAuthor(message.author.username(),
                        "https://discordapp.com/channels/${message.guild.id}/${message.channel.id}/${message.id}",
                        message.author.effectiveAvatarUrl)
        if (!message.attachments.isEmpty()) {
            val attachment = message.attachments[0]
            if (attachment.width > 0 || attachment.height > 0) {
                builder.setImage(attachment.url)
            }
        } else if (!message.embeds.isEmpty()) {
            val messageEmbed = message.embeds[0]
            if (messageEmbed.type == EmbedType.IMAGE) {
                builder.setImage(messageEmbed.thumbnail.url)
            }
        }

        return builder.build()
    }

    override fun execute(MC: MessageContext, args: List<String>) {
        if (MC.serverCtx == null)
            throw IllegalArgumentException("Required to be run on a server")

        val countArg = args.getOrNull(0)
                ?: throw IllegalArgumentException("Count argument is required.")
        val channelArg = args.getOrNull(0)
                ?: throw IllegalArgumentException("Channel argument is required.")

        try {
            val count = countArg.toInt()
            val matches = channelRegex.find(channelArg)
            val channelId = matches?.groups?.get(1)?.value

            if (channelId != null) {
                val messages = MC.channel.iterableHistory.filter { !it.author.isBot }
                        .take(count)
                val textChannel = MC.serverCtx.guild.getTextChannelById(channelId)
                if (textChannel != null) {
                    // TODO: queue chain created through andThen Consumers
                    messages.reversed()
                            .map { transform(it, MC.userCtx.user) }
                            .forEach { textChannel.sendMessage(it).queue() }
                } else {
                    throw IllegalArgumentException("Did not find the channel specified.")
                }
            }
        } catch (_: NumberFormatException) {
             throw IllegalArgumentException("Failed to parse the number of messages to move.")
        }
    }
}