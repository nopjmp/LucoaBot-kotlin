package moe.giga.discord.commands

import moe.giga.discord.annotations.IsCommand
import moe.giga.discord.contexts.MessageContext
import moe.giga.discord.permissions.AccessLevel

@IsCommand()
class RoleSpecCommand : Command() {
    override val name = "rolespec"
    override val usage = "rolespec <role spec> <role name>"
    override val level = AccessLevel.ADMIN

    override fun onCommand(MC: MessageContext, args: List<String>) {
        try {
            val roleSpec = args.first()
            val roleName = args[1]

            val found = MC.serverCtx.guild.roles.find { it.name.compareTo(roleName, true) == 0 }

            if (found == null) {
                MC.sendError("Could not find the role `$roleName` on this server.")
            } else {
                if (roleSpec != "mod" || roleSpec != "admin") {
                    MC.sendError("You can only use `mod` or `admin` for the role spec.")
                } else {
                    MC.serverCtx.addSpecRole(roleSpec, roleName)
                    MC.sendMessage("Role $roleName => $roleSpec permissions")
                }
            }
        } catch (_: Exception) {
            MC.sendError("Arguments are incorrect.")
        }
    }
}