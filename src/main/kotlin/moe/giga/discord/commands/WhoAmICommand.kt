package moe.giga.discord.commands

import moe.giga.discord.annotations.IsCommand
import moe.giga.discord.contexts.MessageContext

@IsCommand()
class WhoAmICommand : Command() {
    override val name = "whoami"
    override val hidden = true

    override fun onCommand(MC: MessageContext, args: List<String>) {
        MC.sendMessage("You are **${MC.userCtx.humanRole}**").queue()
    }
}
