package moe.giga.discord.commands

import moe.giga.discord.annotations.IsCommand
import moe.giga.discord.contexts.MessageContext
import moe.giga.discord.permissions.AccessLevel

@IsCommand
class AddRoleCommand : Command() {
    override val name = "addrole"
    override val aliases = arrayOf("asar")
    override val description = "Adds role(s) to the self assignable role list."
    override val usage = "addrole <role> <group>"
    override val level = AccessLevel.MOD

    override fun onCommand(MC: MessageContext, args: List<String>) {
        val roleName = args.first()
        val group = args.getOrElse(1, { "default" })

        val foundRole = MC.serverCtx.guild.roles.find { it.name.compareTo(roleName, true) == 0 }
        if (foundRole != null) {
            MC.serverCtx.addSelfRole(group, foundRole.id)

            MC.sendMessage("**${foundRole.name}** is now self assignable in group $group.").queue()
        } else {
            MC.sendError("`%s` not found as a role on this server.", roleName).queue()
        }
    }
}