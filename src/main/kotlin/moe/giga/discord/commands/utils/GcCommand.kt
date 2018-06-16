package moe.giga.discord.commands.utils

import moe.giga.discord.commands.Command
import moe.giga.discord.contexts.MessageContext
import moe.giga.discord.util.AccessLevel
import org.pmw.tinylog.Logger

@Suppress("unused")
class GcCommand : Command {
    override val name = "gc"
    override val hidden = true
    override val level = AccessLevel.ROOT

    override fun execute(MC: MessageContext, args: List<String>) {
        Logger.info("System.gc() invoked by GcCommand")
        MC.sendMessage("Invoking System.gc()").queue()
        System.gc()
    }
}