package moe.giga.discord.commands.custom

import moe.giga.discord.Database
import moe.giga.discord.commands.Command
import moe.giga.discord.contexts.MessageContext
import moe.giga.discord.util.AccessLevel
import java.sql.SQLException

@Suppress("unused")
class RemoveCustomCommand : Command {
    override val name = "removecustom"
    override val aliases = arrayOf("rc")
    override val description = "Removes a custom command"
    override val level = AccessLevel.MOD

    companion object {
        const val DELETE_CUSTOM_COMMAND = "customCommandDeleteOp"
    }

    override fun execute(MC: MessageContext, args: List<String>) {
        val command = args.getOrNull(0)
                ?: throw IllegalArgumentException("You must supply a command to remove.")

        if (MC.serverCtx.guild == null)
            throw IllegalArgumentException("You can only use this command on a server.")

        try {
            Database.withStatement(DELETE_CUSTOM_COMMAND) {
                setString(1, MC.serverCtx.guild.id)
                setString(2, command)
                executeUpdate()
                MC.sendMessage("Deleted command `$command`.").queue()
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }
}