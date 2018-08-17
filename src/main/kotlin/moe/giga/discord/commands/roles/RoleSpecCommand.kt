package moe.giga.discord.commands.roles

import moe.giga.discord.commands.Command
import moe.giga.discord.contexts.MessageContext
import moe.giga.discord.util.AccessLevel

@Suppress("unused")
class RoleSpecCommand : Command {
    override val name = "rolespec"
    override val description = "Sets what role gives what permissions (role specification)."
    override val usage = "rolespec <role spec> <role name>"
    override val level = AccessLevel.ADMIN

    private val validSpecs = listOf("mod", "admin")

    override fun execute(MC: MessageContext, args: List<String>) {
        if (MC.server == null)
            throw IllegalArgumentException("You can only use this command on a server.")

        val roleSpec = args.first()
        val roleName = args[1]

        if (!validSpecs.contains(roleSpec)) {
            MC.sendError("You can only use `mod` or `admin` for the role spec.").queue()
        } else {
            val role = MC.server.guild.roles.find { it.name.compareTo(roleName, true) == 0 }
                    ?: throw IllegalArgumentException("`$roleName` not role as a role on this server.")
            MC.server.addSpecRole(roleSpec, role.idLong)
            MC.sendMessage("Role ${role.name} => $roleSpec permissions").queue()
        }
    }
}