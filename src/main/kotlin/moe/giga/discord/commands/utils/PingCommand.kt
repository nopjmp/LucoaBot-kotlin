package moe.giga.discord.commands.utils

import moe.giga.discord.commands.Command
import moe.giga.discord.contexts.MessageContext

@Suppress("unused")
class PingCommand : Command {
    override val name = "ping"
    override val description = "Ping command"

    override fun execute(MC: MessageContext, args: List<String>) {
        MC.channel.sendMessage("Pong! ${MC.jda.ping}ms").queue()
    }
}
