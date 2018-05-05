package moe.giga.discord.commands

import moe.giga.discord.annotations.IsCommand
import moe.giga.discord.contexts.MessageContext
import moe.giga.discord.permissions.AccessLevel
import org.pmw.tinylog.Logger

@IsCommand
class GcCommand : Command() {
    override val name = "gc"
    override val hidden = true
    override val level = AccessLevel.ROOT

    override fun onCommand(MC: MessageContext, args: List<String>) {
        Logger.info("System.gc() invoked by GcCommand!!!")
        MC.sendMessage("Invoking System.gc()").queue()
        System.gc()
    }
}