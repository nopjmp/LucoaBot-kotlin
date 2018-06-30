package moe.giga.discord.commands.custom

import moe.giga.discord.Database
import moe.giga.discord.LucoaBot
import moe.giga.discord.commands.Command
import moe.giga.discord.contexts.MessageContext
import moe.giga.discord.util.AccessLevel
import java.sql.SQLException

@Suppress("unused")
class AddCustomCommand : Command {
    override val name = "addcustom"
    override val aliases = arrayOf("ac")
    override val usage = "addcustom <command> <response>"
    override val description = "Adds custom commands to be used by anyone"
    override val level = AccessLevel.MOD

    companion object {
        const val ADD_CUSTOM_COMMAND = "customCommandAddOp"
    }

    override fun execute(MC: MessageContext, args: List<String>) {
        val command = args.getOrNull(0)
                ?: throw IllegalArgumentException("You must supply a command and response.")

        val response = args.drop(1).joinToString(" ")
        if (response.isEmpty())
            throw IllegalArgumentException("You must supply a command response.")

        if (LucoaBot.handler.hasCommand(command))
            throw IllegalArgumentException("You cannot use a command that already exists as a bot command.")

        if (MC.serverCtx.guild == null)
            throw IllegalArgumentException("You can only use this command on a server.")

        try {
            Database.withStatement(ADD_CUSTOM_COMMAND) {
                setString(1, MC.serverCtx.guild.id)
                setString(2, command)
                setString(3, response)
                executeUpdate()
                MC.sendMessage("Added command `$command`.").queue()
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }
}