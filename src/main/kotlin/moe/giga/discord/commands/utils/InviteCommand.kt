package moe.giga.discord.commands.utils

import moe.giga.discord.SettingsManager
import moe.giga.discord.commands.Command
import moe.giga.discord.contexts.MessageContext

@Suppress("unused")
class InviteCommand : Command {
    override val name = "invite"
    override val description = "Provides a link to invite LucoaBot to your own server!"

    companion object {
        const val authorizeUrl = "https://discordapp.com/api/oauth2/authorize"
    }

    override fun execute(MC: MessageContext, args: List<String>) {
        MC.sendMessage("$authorizeUrl?client_id=${SettingsManager.instance.settings.clientId}&permissions=8&scope=bot").queue()
    }

}