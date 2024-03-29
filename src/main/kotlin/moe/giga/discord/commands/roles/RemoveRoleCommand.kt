package moe.giga.discord.commands.roles

import moe.giga.discord.commands.Command
import moe.giga.discord.contexts.MessageContext
import moe.giga.discord.util.AccessLevel

@Suppress("unused")
class RemoveRoleCommand : Command {
    override val name = "removerole"
    override val aliases = arrayOf("rsar")
    override val description = "Removes role(s) from the self assignable role list."
    override val usage = "removerole <role>"
    override val level = AccessLevel.MOD

    override fun execute(MC: MessageContext, args: List<String>) {
        if (MC.server == null)
            throw IllegalArgumentException("You can only use this command on a server.")

        val roleName = args.first()

        val foundRole = MC.server.guild.roles.find { it.name.compareTo(roleName, true) == 0 }
                ?: throw IllegalArgumentException("`$roleName` not found as a role on this server.")

        if (MC.server.getServerSelfRoles().count { it.value.contains(foundRole.idLong) } > 0) {
            MC.server.deleteSelfRole(foundRole.idLong)

            MC.sendMessage("**${foundRole.name}** is no longer self assignable.").queue()
        } else {
            throw IllegalArgumentException("`$roleName` not found as a self-assignable role on this server.")
        }
    }
}