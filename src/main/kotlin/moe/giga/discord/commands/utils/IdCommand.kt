package moe.giga.discord.commands.utils

import moe.giga.discord.commands.Command
import moe.giga.discord.contexts.MessageContext

@Suppress("unused")
class IdCommand : Command {
    override val name = "id"
    override val hidden = true

    override fun execute(MC: MessageContext, args: List<String>) {
        MC.sendMessage("ID: ${MC.user.user.id}").queue()
    }
}
