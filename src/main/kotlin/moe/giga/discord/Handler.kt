package moe.giga.discord

import moe.giga.discord.commands.Command
import moe.giga.discord.contexts.MessageContext
import moe.giga.discord.contexts.ServerContext
import net.dv8tion.jda.core.MessageBuilder
import net.dv8tion.jda.core.entities.Game
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.events.ReadyEvent
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.hooks.SubscribeEvent
import org.pmw.tinylog.Logger
import kotlin.concurrent.timer

class Handler internal constructor(val commands: List<Command>) {
    private val commandMap = commands.associateBy { it.name }
    private val aliasMap = mutableMapOf<String, Command>()

    // TODO: add ability to escape double quotes
    private val splitRegex = Regex("([\"'])((?:\\\\\\1|.)*?)\\1|[^ '\"]+")

    init {
        commands.filter { it.aliases.isNotEmpty() }
                .forEach { command ->
                    command.aliases.forEach { aliasMap[it] = command }
                }
    }

    companion object {
        const val DEFAULT_PREFIX = "."
    }

    fun hasCommand(name: String) =
            commandMap.containsKey(name) or aliasMap.containsKey(name)

    private fun commandArgs(message: Message) = commandArgs(message.contentRaw)

    private fun commandArgs(string: String) = splitRegex.findAll(string)
            .map { it.value.removeSurrounding("\"") }

    private fun resolveCommand(commandName: String) = commandMap[commandName] ?: aliasMap[commandName]

    // TODO: allow multiple word commands, probably will need a trie structure to find the commands, or regex lol
    @SubscribeEvent
    fun handleMessage(event: MessageReceivedEvent) {
        LucoaBot.statistics.messages.incrementAndGet()
        if (!event.isWebhookMessage && event.author != event.jda.selfUser && !event.author.isBot) {
            val serverContext = event.guild?.let { ServerContext(it) }

            val args = commandArgs(event.message)
            val rawCommand = args.firstOrNull() ?: ""

            val prefix = serverContext?.prefix ?: DEFAULT_PREFIX

            if (rawCommand.startsWith(prefix)) {
                val commandName = rawCommand.substring(prefix.length)
                if (hasCommand(commandName))
                    processCommand(MessageContext(event, serverContext),
                            resolveCommand(commandName)!!, args.drop(1).toList())
                else if (serverContext != null) // custom commands only work on servers
                    processCustom(event, commandName, serverContext)
            }
        }
    }

    private fun processCommand(mc: MessageContext, command: Command, args: List<String>) {
        if (mc.user.allowed(command.level)) {
            LucoaBot.statistics.processedCommands.incrementAndGet()
            try {
                command.execute(mc, args)
            } catch (e: IllegalArgumentException) {
                mc.sendError(e.message ?: "Invalid Arguments").queue()
            } catch (e: Exception) {
                Logger.warn("Command exception: ${e.message ?: "(null)"}")
                mc.sendError("Unknown Exception running command.").queue()
            }
        } else {
            mc.sendError("You are not allowed to run `${command.name}`.").queue()
        }
    }

    private fun processCustom(event: MessageReceivedEvent, name: String, serverContext: ServerContext) {
        serverContext.findCustomCommand(name)?.let {
            LucoaBot.statistics.processedCommands.incrementAndGet()
            event.channel.sendMessage(MessageBuilder().append(it)
                    .stripMentions(event.jda, Message.MentionType.EVERYONE, Message.MentionType.HERE)
                    .build()).queue()
        }
    }

    @SubscribeEvent
    fun handleReady(event: ReadyEvent) {
        val jda = event.jda
        timer(name = "activityUpdate", daemon = true, period = 10000, action = {
            val count = jda.guildCache.map { it.memberCache.count() }.sum()
            jda.presence.game = Game.watching("$count users")
        })
    }
}
