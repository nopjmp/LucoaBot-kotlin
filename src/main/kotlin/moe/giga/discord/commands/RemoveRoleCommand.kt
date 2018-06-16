package moe.giga.discord.commands

import moe.giga.discord.contexts.MessageContext
import moe.giga.discord.util.AccessLevel

@Suppress("unused")
class RemoveRoleCommand : Command {
    override val name = "removerole"
    override val alias = "rsar"
    override val description = "Removes role(s) from the self assignable role list."
    override val usage = "removerole <role>"
    override val level = AccessLevel.MOD

    override fun execute(MC: MessageContext, args: List<String>) {
        if (MC.serverCtx.guild == null)
            throw IllegalArgumentException("You can only use this command on a server.")

        val roleName = args.first()

        val foundRole = MC.serverCtx.guild.roles.find { it.name.compareTo(roleName, true) == 0 }
                ?: throw IllegalArgumentException("`$roleName` not found as a role on this server.")

        if (MC.serverCtx.getServerSelfRoles().count { it.value.contains(foundRole.id) } > 0) {
            MC.serverCtx.deleteSelfRole(foundRole.id)

            MC.sendMessage("**${foundRole.name}** is no longer self assignable.").queue()
        } else {
            throw IllegalArgumentException("`$roleName` not found as a self-assignable role on this server.")
        }
    }
}