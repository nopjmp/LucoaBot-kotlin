package moe.giga.discord.contexts

import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.MessageBuilder
import net.dv8tion.jda.core.entities.ChannelType
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.MessageChannel
import net.dv8tion.jda.core.entities.MessageEmbed
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.requests.restaction.MessageAction
import javax.annotation.CheckReturnValue

class MessageContext(event: MessageReceivedEvent, val server: ServerContext?) {
    val channel: MessageChannel = when {
        event.isFromType(ChannelType.PRIVATE) -> event.privateChannel
        else -> event.textChannel
    }

    val user: UserContext = UserContext(event.author, server)
    val jda: JDA = event.jda

    val message = event.message!!

    private fun sendMessage(message: Message): MessageAction = channel.sendMessage(message)

    @CheckReturnValue
    fun sendMessage(message: String): MessageAction = sendMessage(MessageBuilder().append(message).build())

    @CheckReturnValue
    fun sendMessage(message: MessageEmbed): MessageAction = channel.sendMessage(message)

    @CheckReturnValue
    fun sendError(message: String): MessageAction = sendMessage("⛔ $message")
}
