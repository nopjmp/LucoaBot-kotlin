package moe.giga.discord.commands

import moe.giga.discord.LucoaBot
import moe.giga.discord.annotations.IsCommand
import moe.giga.discord.contexts.MessageContext
import net.dv8tion.jda.core.EmbedBuilder

@IsCommand()
class CommandsCommand : Command() {
    override val name = "commands"
    override val description = "Shows avaliable commands"
    override val usage = "commands"

    override fun execute(MC: MessageContext, args: List<String>) {
        val embedBuilder = EmbedBuilder().setTitle("Bot Commands")
        fun generateField(command: Command) {
            val sb = StringBuilder()
            command.alias?.let {
                sb.append("*Alias: $it*\n")
            }

            sb.append("*Access Level: ${command.level}*\n")
            sb.append(command.description + "\n")
            embedBuilder.addField(MC.serverCtx.prefix + command.usage, sb.toString(), false)
        }

        LucoaBot.handler.commands
                .filter { !it.hidden && MC.userCtx.allowed(it.level) }
                .sortedBy { it.name }
                .forEach(::generateField)

        MC.sendMessage(embedBuilder.build()).queue()
    }
}
