package moe.giga.discord.commands

import moe.giga.discord.annotations.IsCommand
import moe.giga.discord.contexts.MessageContext

@IsCommand
class IamCommand : Command() {
    override val name = "iam"
    override val alias = "r"
    override val description = "Assigns roles to the user running this command."
    override val usage = "iam <role>"

    override fun execute(MC: MessageContext, args: List<String>) {
        if (MC.serverCtx.guild == null)
            throw IllegalArgumentException("You can only use this command on a server.")

        val roleName = args.joinToString(separator = " ")
        val foundRoles = MC.serverCtx.guild.getRolesByName(roleName, true)
        val role = foundRoles.firstOrNull()
                ?: throw IllegalArgumentException("`$roleName` not found as a role on this server.")

        val selfRoles = MC.serverCtx.getServerSelfRoles()
        val key = selfRoles
                .filterValues { it.contains(role.id) }
                .map { it.key }
                .first()

        val member = MC.userCtx.member ?: throw IllegalArgumentException("Internal Error: Member not found on server.")
        if (member.roles.contains(role)) {
            MC.sendError("${MC.userCtx.asText}... You already have **${role.name}**.").queue()
        } else {
            val controller = MC.serverCtx.guild.controller

            if (key != "default") {
                val filtered = member.roles.filter { selfRoles.getValue(key).contains(it.id) }
                controller.removeRolesFromMember(member, filtered).queue()
            }

            controller.addRolesToMember(member, role).queue({
                MC.sendMessage("${MC.userCtx.asText} now has the role **${role.name}**").queue()
            })
        }
    }
}