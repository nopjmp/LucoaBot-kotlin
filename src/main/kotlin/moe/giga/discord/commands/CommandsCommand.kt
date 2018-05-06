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

    override fun onCommand(MC: MessageContext, args: List<String>) {
        val embedBuilder = EmbedBuilder().setTitle("Bot Commands")
        LucoaBot.handler.commands.filter { !it.hidden }.forEach {
            embedBuilder.addField(MC.serverCtx.prefix + it.usage, it.description, false)
        }

        MC.sendMessage(embedBuilder.build()).queue()
    }
}
