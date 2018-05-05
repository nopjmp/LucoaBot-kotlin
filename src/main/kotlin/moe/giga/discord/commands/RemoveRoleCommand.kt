package moe.giga.discord.commands

import moe.giga.discord.annotations.IsCommand
import moe.giga.discord.contexts.MessageContext

@IsCommand
class RemoveRoleCommand : Command() {
    override val name = "removerole"
    override val aliases = arrayOf("rsar")
    override val description = "Removes role(s) from the self assignable role list."
    override val usage = "removerole <role>"

    override fun onCommand(MC: MessageContext, args: List<String>) {
        val roleName = args.first()

        val foundRole = MC.serverCtx.guild.roles.find { it.name.compareTo(roleName, true) == 0 }
        if (foundRole != null) {
            if (MC.serverCtx.getServerSelfRoles().count { it.value.contains(foundRole.id) } > 0) {
                MC.serverCtx.deleteSelfRole(foundRole.id)

                MC.sendFormattedMessage("**%s** is no longer self assignable.", foundRole.name).queue()
                return
            }
        }

        MC.sendError("`%s` not found as a role on this server.", roleName).queue()
    }
}