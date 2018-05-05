package moe.giga.discord.commands

import moe.giga.discord.annotations.IsCommand
import moe.giga.discord.contexts.MessageContext

@IsCommand
class IamCommand : Command() {
    override val name = "iam"
    override val aliases = arrayOf("r")
    override val description = "Assigns roles to the user running this command."
    override val usage = "iam <role>"

    override fun onCommand(MC: MessageContext, args: List<String>) {
        val roleName = args.joinToString(separator = " ")
        try {
            val foundRoles = MC.serverCtx.guild.getRolesByName(roleName, true)
            if (foundRoles.count() > 0) {
                val role = foundRoles.first()

                val selfRoles = MC.serverCtx.getServerSelfRoles()
                val key = selfRoles
                        .filterValues { it.contains(role.id) }
                        .map { it.key }
                        .first()

                val member = MC.userCtx.member
                if (member != null) {
                    if (member.roles.contains(role)) {
                        MC.sendError("%s... You already have **%s**.", MC.userCtx.asText, role.name).queue()
                    } else {

                        val controller = MC.serverCtx.guild.controller

                        if (key != "default") {
                            val filtered = member.roles.filter { selfRoles.getValue(key).contains(it.id) }
                            controller.removeRolesFromMember(member, filtered).queue()
                        }

                        controller.addRolesToMember(member, role).queue({
                            MC.sendFormattedMessage("%s now has the role **%s**", MC.userCtx.asText, role.name).queue()
                        })
                    }
                }
            }

            return
        } catch (e: NoSuchElementException) {
            // fall through
        }

        MC.sendError("`%s` not found as a role on this server.", roleName).queue()
    }
}