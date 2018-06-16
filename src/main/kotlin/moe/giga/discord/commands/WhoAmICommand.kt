package moe.giga.discord.commands

import moe.giga.discord.contexts.MessageContext

@Suppress("unused")
class WhoAmICommand : Command {
    override val name = "whoami"
    override val hidden = true

    override fun execute(MC: MessageContext, args: List<String>) {
        MC.sendMessage("You are **${MC.userCtx.humanRole}**").queue()
    }
}
