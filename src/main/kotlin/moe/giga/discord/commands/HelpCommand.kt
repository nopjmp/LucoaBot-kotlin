package moe.giga.discord.commands

import moe.giga.discord.LucoaBot
import moe.giga.discord.contexts.MessageContext
import net.dv8tion.jda.core.EmbedBuilder

// TODO: refactor this into commands and help with "about the bot" in the help command
@Suppress("unused")
class HelpCommand : Command {
    override val name = "help"
    override val aliases = arrayOf("commands")
    override val description = "Shows avaliable commands"
    override val usage = "commands [command name or all [text (defaults to embed)]]"

    override fun execute(MC: MessageContext, args: List<String>) {
        fun generateField(command: Command): Pair<String, String> {
            val sb = StringBuilder()
            if (command.aliases.isNotEmpty()) {
                command.aliases.let {
                    sb.append("*Aliases: ${it.joinToString()}*\n")
                }
            }

            sb.append("*Access Level: ${command.level}*\n")
            sb.append(command.description + "\n")
            return Pair(MC.serverCtx.prefix + command.usage, sb.toString())
        }

        val arg = args.getOrNull(0)
        val search = when (arg) {
            null -> ""
            "all" -> ""
            else -> arg
        }

        val textOnly = (args.getOrNull(1) ?: "").compareTo("text", true) == 0
        val initHelpPairs = LucoaBot.handler.commands
                .filter { !it.hidden && MC.userCtx.allowed(it.level) }
                .sortedBy { it.name }

        val helpPairs = when (search) {
            "" -> initHelpPairs
            else -> initHelpPairs.filter { it.name == search || it.aliases.contains(search) }
        }

        if (helpPairs.isEmpty()) {
            MC.sendMessage("Command `$search` not found!").queue()
        } else {
            if (textOnly) {
                val sb = StringBuilder()
                helpPairs.map(::generateField).forEach { sb.append("**${it.first}**\n${it.second}\n") }

                MC.sendMessage(sb.toString()).queue()
            } else {
                val embedBuilder = EmbedBuilder().setTitle("Bot Commands")
                helpPairs.map(::generateField).forEach { embedBuilder.addField(it.first, it.second, false) }

                MC.sendMessage(embedBuilder.build()).queue()
            }
        }
    }
}
