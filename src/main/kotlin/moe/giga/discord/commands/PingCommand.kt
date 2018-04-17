package moe.giga.discord.commands

import moe.giga.discord.annotations.IsCommand
import moe.giga.discord.contexts.MessageContext
import java.time.Duration
import java.time.Instant

@IsCommand
class PingCommand : Command() {
    override val name = "ping"
    override val description = "Ping command"

    override fun onCommand(MC: MessageContext, args: Array<String>) {
        val start = Instant.now()
        MC.channel.sendMessage("Pong!").queue {
            val end = Instant.now()
            val delta = Duration.between(start, end).toMillis()
            it.editMessage(String.format("Pong! %dms", delta)).queue()
        }
    }
}
