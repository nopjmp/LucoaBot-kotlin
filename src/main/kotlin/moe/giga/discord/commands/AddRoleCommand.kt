package moe.giga.discord.commands

import moe.giga.discord.annotations.IsCommand
import moe.giga.discord.contexts.MessageContext

@IsCommand
class AddRoleCommand : Command() {
    override val name = "addrole"
    override val aliases = arrayOf("asar")
    override val description = "Assigns roles to the user running this command."
    override val usage = "addrole <role>"

    override fun onCommand(MC: MessageContext, args: List<String>) {
        val roleName = args.first()
        val group = args.getOrElse(1, { "default" })
    }
}