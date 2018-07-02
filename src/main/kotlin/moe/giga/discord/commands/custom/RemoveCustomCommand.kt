package moe.giga.discord.commands.custom

import kotliquery.HikariCP
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import moe.giga.discord.commands.Command
import moe.giga.discord.contexts.MessageContext
import moe.giga.discord.util.AccessLevel

@Suppress("unused")
class RemoveCustomCommand : Command {
    override val name = "removecustom"
    override val aliases = arrayOf("rc")
    override val description = "Removes a custom command"
    override val level = AccessLevel.MOD

    companion object {
        const val DELETE_CUSTOM_COMMAND = "DELETE FROM custom_commands WHERE server_id = ? AND command = ?"
    }

    override fun execute(MC: MessageContext, args: List<String>) {
        val command = args.getOrNull(0)
                ?: throw IllegalArgumentException("You must supply a command to remove.")

        if (MC.serverCtx == null)
            throw IllegalArgumentException("You can only use this command on a server.")

        using(sessionOf(HikariCP.dataSource())) { session ->
            session.run(queryOf(DELETE_CUSTOM_COMMAND, MC.serverCtx.guild.id, command).asUpdate)
        }
        MC.sendMessage("Deleted command `$command`.").queue()
    }
}