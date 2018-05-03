package moe.giga.discord.commands

import moe.giga.discord.annotations.IsCommand
import moe.giga.discord.contexts.MessageContext

@IsCommand
class IamNotCommand : Command() {
    override val name = "iamnot"
    override val description = "Removes roles from the user running this command."
    override val usage = "iamnot <role>"

    override fun onCommand(MC: MessageContext, args: List<String>) {
        val roleName = args.joinToString(separator = " ")

        val foundRoles = MC.serverCtx.guild.getRolesByName(roleName, true)
        if (foundRoles.count() > 0) {
            val role = foundRoles.first()

            if (MC.serverCtx.getServerSelfRoles()
                            .filterValues { it.contains(role.id) }
                            .isNotEmpty()) {
                val member = MC.userCtx.member
                if (member != null) {
                    if (!member.roles.contains(role)) {
                        MC.sendError("%s... You don't already have **%s**.", MC.userCtx.asText, role.name)
                    } else {
                        MC.serverCtx.guild.controller.removeRolesFromMember(member, role).queue({
                            MC.sendFormattedMessage("%s no longer has **%s**", MC.userCtx.asText, role.name)
                        })
                    }
                }
                return
            }
        }

        MC.sendError("`%s` not found as a role on this server.", roleName).queue()
    }
}