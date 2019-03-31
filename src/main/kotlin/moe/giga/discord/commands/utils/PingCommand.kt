package moe.giga.discord.commands.utils

import moe.giga.discord.commands.Command
import moe.giga.discord.contexts.MessageContext

@Suppress("unused")
class PingCommand : Command {
    override val name = "ping"
    override val description = "Ping command"

    override fun execute(MC: MessageContext, args: List<String>) {
//        val start = Instant.now()
//        MC.channel.sendMessage("Pong!").queue {
//            val end = Instant.now()
//            val delta = Duration.between(start, end).toMillis()
//            it.editMessage(String.format("Pong! %dms", delta)).queue()
//        }
        MC.channel.sendMessage(String.format("Pong! %dms", MC.jda.ping)).queue()
    }
}
