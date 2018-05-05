package moe.giga.discord.contexts

import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.MessageBuilder
import net.dv8tion.jda.core.entities.*
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.requests.restaction.MessageAction
import javax.annotation.CheckReturnValue

class MessageContext private constructor(
        val channel: MessageChannel,
        val userCtx: UserContext,
        val serverCtx: ServerContext,
        val jda: JDA
) {

    private constructor(builder: Builder)
            : this(builder.channel, UserContext(builder.user, builder.server), builder.server, builder.jda)

    private fun sendMessage(message: Message): MessageAction = channel.sendMessage(message)

    @CheckReturnValue
    fun sendMessage(message: String): MessageAction = sendMessage(MessageBuilder().append(message).build())

    @CheckReturnValue
    fun sendMessage(message: MessageEmbed): MessageAction = channel.sendMessage(message)

    @CheckReturnValue
    fun sendFormattedMessage(format: String, vararg args: String): MessageAction = sendMessage(String.format(format, *args))

    @CheckReturnValue
    fun sendError(format: String, vararg args: String): MessageAction = sendMessage(String.format("⛔ $format", *args))

    class Builder {
        lateinit var user: User
        lateinit var guild: Guild
        lateinit var jda: JDA

        lateinit var channel: MessageChannel

        lateinit var server: ServerContext

        fun event(event: MessageReceivedEvent): Builder {
            user = event.author
            guild = event.guild
            jda = event.jda
            channel = when {
                event.isFromType(ChannelType.PRIVATE) -> event.privateChannel
                else -> event.textChannel
            }
            return this
        }

        fun serverContext(serverContext: ServerContext): Builder {
            this.server = serverContext
            return this
        }

        fun build() = MessageContext(this)
    }
}
