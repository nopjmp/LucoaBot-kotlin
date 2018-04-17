package moe.giga.discord

import moe.giga.discord.commands.Command
import moe.giga.discord.contexts.MessageContext
import moe.giga.discord.contexts.ServerContext
import net.dv8tion.jda.core.JDABuilder
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.hooks.SubscribeEvent

class CommandHandler internal constructor(builder: JDABuilder, commands: List<Command>) {
    private val commandMap: Map<String, Command> = commands.associateBy { it.name }
    private val aliasMap: MutableMap<String, String> = HashMap()

    init {
        for (command in commands) {
            command.init(builder)

            // TODO: find a nicer way to do this
            if (command.aliases.isNotEmpty())
                command.aliases.forEach { aliasMap.putIfAbsent(it, command.name) }
        }
    }

    private fun containsCommand(SC: ServerContext, message: Message): Boolean {
        val command = commandArgs(message).first()
        return command.startsWith(SC.prefix) && commandMap.containsKey(command.substring(1))
    }

    private fun commandArgs(message: Message): List<String> {
        return commandArgs(message.contentRaw)
    }

    private fun commandArgs(string: String): List<String> {
        return string.split(" ")
    }

    private fun resolveCommand(lookupString: String): Command? {
        val commandName = aliasMap.getOrDefault(lookupString, lookupString)
        return commandMap[commandName]
    }

    // TODO: allow multiple word commands, probably will need a trie structure to find the commands, or regex lol
    @SubscribeEvent
    fun handleMessage(event: MessageReceivedEvent) {
        val serverContext = ServerContext(event.guild)
        if (containsCommand(serverContext, event.message)) {
            val args = commandArgs(event.message)
            val command = resolveCommand(args[0].substring(1))

            if (command != null) {
                if (!event.author.isBot || command.allowBots) {
                    val mc = MessageContext.Builder().event(event).serverContext(serverContext).build()
                    if (mc.userCtx.allowed(command.level)) {
                        command.onCommand(mc, args.drop(1).toTypedArray())
                    } else {
                        mc.sendError("You are not allowed to run `%s`.").queue()
                    }
                }
            }
        }
    }
}
