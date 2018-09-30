package moe.giga.discord.commands.custom

import kotliquery.queryOf
import kotliquery.using
import moe.giga.discord.DB
import moe.giga.discord.LucoaBot
import moe.giga.discord.commands.Command
import moe.giga.discord.contexts.MessageContext
import moe.giga.discord.util.AccessLevel

@Suppress("unused")
class AddCustomCommand : Command {
    override val name = "addcustom"
    override val aliases = arrayOf("ac")
    override val usage = "addcustom <command> <response>"
    override val description = "Adds custom commands to be used by anyone"
    override val level = AccessLevel.MOD

    companion object {
        const val ADD_CUSTOM_COMMAND = "INSERT INTO custom_commands (server_id, command, response) VALUES (?, ?, ?)"
        const val FIND_CUSTOM_COMMAND = "SELECT command FROM custom_commands WHERE server_id = ? AND command = ?"
    }

    override fun execute(MC: MessageContext, args: List<String>) {
        val command = args.getOrNull(0)
                ?: throw IllegalArgumentException("You must supply a command and response.")

        val response = MC.message.contentRaw.split(" ").drop(2).joinToString(" ")
        if (response.isEmpty())
            throw IllegalArgumentException("You must supply a command response.")

        if (LucoaBot.handler.hasCommand(command))
            throw IllegalArgumentException("You cannot use a command that already exists as a bot command.")

        if (MC.server == null)
            throw IllegalArgumentException("You can only use this command on a server.")

        using(DB.session) { session ->
            if (session.single(queryOf(FIND_CUSTOM_COMMAND, MC.server.guildId, command)) { it.anyOrNull(1) } == null)
                session.run(queryOf(ADD_CUSTOM_COMMAND, MC.server.guildId, command, response).asUpdate)
            else
                throw IllegalArgumentException("You cannot use a command that already exists as a custom command.")
        }
        MC.sendMessage("Added command `$command`.").queue()
    }
}