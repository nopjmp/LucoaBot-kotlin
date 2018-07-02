package moe.giga.discord.commands.roles

import moe.giga.discord.commands.Command
import moe.giga.discord.contexts.MessageContext

@Suppress("unused")
class IamNotCommand : Command {
    override val name = "iamnot"
    override val aliases = arrayOf("iamn", "nr")
    override val description = "Removes roles from the user running this command."
    override val usage = "iamnot <role>"

    override fun execute(MC: MessageContext, args: List<String>) {
        if (MC.serverCtx == null)
            throw IllegalArgumentException("You can only use this command on a server.")

        val roleName = args.joinToString(separator = " ")

        val foundRoles = MC.serverCtx.guild.getRolesByName(roleName, true)
        val role = foundRoles.firstOrNull()
                ?: throw IllegalArgumentException("`$roleName` not found as a role on this server.")

        if (MC.serverCtx.getServerSelfRoles()
                        .filterValues { it.contains(role.idLong) }
                        .isEmpty()) {
            throw IllegalArgumentException("`${role.name}` is not a self-assignable role.")
        }

        val member = MC.userCtx.member
        if (member != null) {
            if (!member.roles.contains(role)) {
                MC.sendError("${MC.userCtx.asText}... You don't already have **${role.name}**.").queue()
            } else {
                MC.serverCtx.guild.controller.removeRolesFromMember(member, role).queue {
                    MC.sendMessage("${MC.userCtx.asText} no longer has **${role.name}**").queue()
                }
            }
        }
    }
}