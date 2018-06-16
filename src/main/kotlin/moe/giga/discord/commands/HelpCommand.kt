package moe.giga.discord.commands

import moe.giga.discord.LucoaBot
import moe.giga.discord.SettingsManager
import moe.giga.discord.contexts.MessageContext
import net.dv8tion.jda.core.EmbedBuilder

// TODO: refactor this into commands and help with "about the bot" in the help command
@Suppress("unused")
class HelpCommand : Command {
    override val name = "help"
    override val alias = "commands"
    override val description = "Shows avaliable commands"
    override val usage = "commands [text (defaults to embed)]"

    companion object {
        const val authorizeUrl = "https://discordapp.com/api/oauth2/authorize"
    }

    override fun execute(MC: MessageContext, args: List<String>) {
        fun generateField(command: Command): Pair<String, String> {
            val sb = StringBuilder()
            command.alias?.let {
                sb.append("*Alias: $it*\n")
            }

            sb.append("*Access Level: ${command.level}*\n")
            sb.append(command.description + "\n")
            return Pair(MC.serverCtx.prefix + command.usage, sb.toString())
        }

        val textOnly = (args.getOrNull(0) ?: "").compareTo("text", true) == 0
        val helpPairs = LucoaBot.handler.commands
                .filter { !it.hidden && MC.userCtx.allowed(it.level) }
                .sortedBy { it.name }
                .map(::generateField)

        if (textOnly) {
            val sb = StringBuilder()
            helpPairs.forEach { sb.append("**${it.first}**\n${it.second}\n") }

            sb.append("**Invite Link**\n" +
                    "$authorizeUrl?client_id=${SettingsManager.instance.settings.clientId}&permissions=8&scope=bot\n")

            MC.sendMessage(sb.toString()).queue()
        } else {
            val embedBuilder = EmbedBuilder().setTitle("Bot Commands")
            helpPairs.forEach { embedBuilder.addField(it.first, it.second, false) }

            embedBuilder.addField("Invite Link",
                    "$authorizeUrl?client_id=${SettingsManager.instance.settings.clientId}&permissions=8&scope=bot", false)

            MC.sendMessage(embedBuilder.build()).queue()
        }
    }
}
