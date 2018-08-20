package moe.giga.discord.commands.roles

import moe.giga.discord.commands.Command
import moe.giga.discord.contexts.MessageContext
import moe.giga.discord.util.AccessLevel

@Suppress("unused")
class AddRoleCommand : Command {
    override val name = "addrole"
    override val aliases = arrayOf("asar")
    override val description = "Adds role(s) to the self assignable role list."
    override val usage = "addrole <role> <group>"
    override val level = AccessLevel.MOD

    override fun execute(MC: MessageContext, args: List<String>) {
        val roleName = args.first()
        val group = args.getOrElse(1) { "default" }

        if (MC.server == null)
            throw IllegalArgumentException("You can only use this command on a server.")

        val foundRole = MC.server.guild.roles.find { it.name.compareTo(roleName, true) == 0 }
                ?: throw IllegalArgumentException("`$roleName` not found as a role on this server.")
        MC.server.addSelfRole(group, foundRole.idLong)
        MC.sendMessage("**${foundRole.name}** is now self assignable in group $group.").queue()
    }
}