package moe.giga.discord.commands

import moe.giga.discord.annotations.IsCommand
import moe.giga.discord.contexts.MessageContext

@IsCommand
class IdCommand : Command() {
    override val name = "id"
    override val hidden = true

    override fun execute(MC: MessageContext, args: List<String>) {
        MC.sendMessage("ID: ${MC.userCtx.user.id}").queue()
    }
}
