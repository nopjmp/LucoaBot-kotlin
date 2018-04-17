package moe.giga.discord.commands

import moe.giga.discord.annotations.IsCommand
import moe.giga.discord.contexts.MessageContext

@IsCommand
class IdCommand : Command() {
    override val name = "id"
    override val hidden = true

    override fun onCommand(MC: MessageContext, args: Array<String>) {
        MC.sendFormattedMessage("ID: %s", MC.userCtx.user.id).queue()
    }
}
