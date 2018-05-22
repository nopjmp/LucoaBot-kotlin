package moe.giga.discord.commands

import moe.giga.discord.LucoaBot
import moe.giga.discord.annotations.IsCommand
import moe.giga.discord.contexts.MessageContext
import moe.giga.discord.util.AccessLevel
import net.dv8tion.jda.core.EmbedBuilder

@IsCommand()
class CommandsCommand : Command() {
    override val name = "commands"
    override val description = "Shows avaliable commands"
    override val usage = "commands"

    private fun fieldBody(command: Command): String {
        val list = mutableListOf<String>()
        if (command.aliases.isNotEmpty())
            list.add("*Aliases: ${command.aliases.joinToString()}*")
        if (command.level != AccessLevel.USER)
            list.add("*Access Level: ${command.level}*")
        list.add(command.description)

        return list.joinToString("\n")
    }

    override fun onCommand(MC: MessageContext, args: List<String>) {
        val embedBuilder = EmbedBuilder().setTitle("Bot Commands")
        LucoaBot.handler.commands
                .filter { !it.hidden && MC.userCtx.allowed(it.level) }
                .sortedBy { it.name }
                .forEach {
                    embedBuilder.addField(MC.serverCtx.prefix + it.usage, fieldBody(it), false)
                }

        MC.sendMessage(embedBuilder.build()).queue()
    }
}
