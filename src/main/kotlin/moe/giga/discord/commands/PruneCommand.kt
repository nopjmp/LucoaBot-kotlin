package moe.giga.discord.commands

import moe.giga.discord.annotations.IsCommand
import moe.giga.discord.contexts.MessageContext
import moe.giga.discord.permissions.AccessLevel
import net.dv8tion.jda.core.entities.ChannelType
import net.dv8tion.jda.core.entities.TextChannel
import java.util.*
import kotlin.concurrent.schedule

@IsCommand()
class PruneCommand : Command() {
    override val name = "prune"
    override val description = "Removes the last X number of messages."
    override val usage = "prune <number of messages>"
    override val level = AccessLevel.MOD

    override fun onCommand(MC: MessageContext, args: List<String>) {
        try {
            val num = Integer.valueOf(args.first())
            when {
                MC.channel.type == ChannelType.TEXT ->
                    MC.channel.history.retrievePast(num + 1).queue {
                        (MC.channel as TextChannel).deleteMessages(it).queue()
                        MC.sendMessage("Bulk deleted $num messages.").queue {
                            Timer().schedule(5000) { it.delete().queue() }
                        }
                    }
                else -> MC.sendError("We can't delete messages here right now.").queue()
            }
        } catch (_: NumberFormatException) {
            MC.sendError("Failed to parse the number of messages you wanted to delete.").queue()
        } catch (_: Exception) {
            MC.sendError("Failed to delete messages.").queue()
        }
    }
}