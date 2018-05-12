package moe.giga.discord.commands

import moe.giga.discord.annotations.IsCommand
import moe.giga.discord.contexts.MessageContext
import moe.giga.discord.util.AccessLevel

@IsCommand()
class PrefixCommand : Command() {
    override val name = "prefix"
    override val description = "Changes the prefix to use for commands."
    override val usage = "prefix <prefix text>"
    override val level = AccessLevel.MOD

    override fun onCommand(MC: MessageContext, args: List<String>) {
        val newPrefix = args.first()
        if (newPrefix.length > 16) {
            MC.sendMessage("Prefix cannot be more than 16 characters.").queue()
            return
        }

        try {
            MC.serverCtx.prefix = newPrefix
            MC.serverCtx.save()

            MC.sendMessage("${MC.userCtx.asText} has changed the prefix to `$newPrefix`.").queue()
        } catch (_: Exception) {
            MC.sendError("Something bad happened while saving the new configuration.").queue()
        }
    }
}
