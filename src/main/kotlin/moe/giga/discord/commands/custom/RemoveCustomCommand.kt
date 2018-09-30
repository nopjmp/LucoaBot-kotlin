package moe.giga.discord.commands.custom

import kotliquery.queryOf
import kotliquery.using
import moe.giga.discord.DB
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
        const val FIND_CUSTOM_COMMAND = "SELECT command FROM custom_commands WHERE server_id = ? AND command = ?"

    }

    override fun execute(MC: MessageContext, args: List<String>) {
        val command = args.getOrNull(0)
                ?: throw IllegalArgumentException("You must supply a command to remove.")

        if (MC.server == null)
            throw IllegalArgumentException("You can only use this command on a server.")

        using(DB.session) { session ->
            if (session.single(queryOf(FIND_CUSTOM_COMMAND, MC.server.guildId, command)) { it.anyOrNull(1) } != null)
                session.run(queryOf(DELETE_CUSTOM_COMMAND, MC.server.guildId, command).asUpdate)
            else
                throw IllegalArgumentException("You can only delete custom commands that exist.")
        }
        MC.sendMessage("Deleted command `$command`.").queue()
    }
}