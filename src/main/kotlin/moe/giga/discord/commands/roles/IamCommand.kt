package moe.giga.discord.commands.roles

import moe.giga.discord.commands.Command
import moe.giga.discord.contexts.MessageContext

@Suppress("unused")
class IamCommand : Command {
    override val name = "iam"
    override val aliases = arrayOf("r")
    override val description = "Assigns roles to the user running this command."
    override val usage = "iam <role>"

    override fun execute(MC: MessageContext, args: List<String>) {
        if (MC.server == null)
            throw IllegalArgumentException("You can only use this command on a server.")

        val roleName = args.joinToString(separator = " ")
        val foundRoles = MC.server.guild.getRolesByName(roleName, true)
        val role = foundRoles.firstOrNull()
                ?: throw IllegalArgumentException("`$roleName` not found as a role on this server.")

        val selfRoles = MC.server.getServerSelfRoles()
        val key = selfRoles
                .filterValues { it.contains(role.idLong) }
                .map { it.key }
                .firstOrNull() ?: throw IllegalArgumentException("`${role.name}` is not a self-assignable role.")

        val member = MC.user.member ?: throw IllegalArgumentException("Internal Error: Member not found on server.")
        if (member.roles.contains(role)) {
            MC.sendError("${MC.user.asText}... You already have **${role.name}**.").queue()
        } else {
            val controller = MC.server.guild.controller

            if (key != "default") {
                val filtered = member.roles.filter { selfRoles.getValue(key).contains(it.idLong) }
                controller.removeRolesFromMember(member, filtered).queue()
            }

            controller.addRolesToMember(member, role).queue {
                MC.sendMessage("${MC.user.asText} now has the role **${role.name}**").queue()
            }
        }
    }
}