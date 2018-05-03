package moe.giga.discord

import moe.giga.discord.commands.Command
import moe.giga.discord.contexts.MessageContext
import moe.giga.discord.contexts.ServerContext
import net.dv8tion.jda.core.JDABuilder
import net.dv8tion.jda.core.entities.Game
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.events.ReadyEvent
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.hooks.SubscribeEvent
import kotlin.concurrent.timer

class Handler internal constructor(builder: JDABuilder, commands: List<Command>) {
    private val commandMap: Map<String, Command> = commands.associateBy { it.name }
    private val aliasMap: MutableMap<String, String> = HashMap()

    // TODO: add ability to escape double quotes
    private val splitRegex = Regex("([\"'])((?:\\\\\\1|.)*?)\\1|[^ '\"]+")

    init {
        for (command in commands) {
            command.init(builder)
            command.aliases.forEach { aliasMap.putIfAbsent(it, command.name) }
        }
    }

    private fun containsCommand(SC: ServerContext, message: Message): Boolean {
        return try {
            val command = commandArgs(message).first()
            command.startsWith(SC.prefix) && commandMap.containsKey(command.substring(1))
        } catch (_: NoSuchElementException) {
            false
        }
    }

    private fun commandArgs(message: Message): Sequence<String> {
        return commandArgs(message.contentRaw)
    }

    private fun commandArgs(string: String): Sequence<String> {
        return splitRegex.findAll(string).map { it.value.removeSurrounding("\"") }
    }

    private fun resolveCommand(lookupString: String): Command? {
        val commandName = aliasMap.getOrDefault(lookupString, lookupString)
        return commandMap[commandName]
    }

    // TODO: allow multiple word commands, probably will need a trie structure to find the commands, or regex lol
    @SubscribeEvent
    fun handleMessage(event: MessageReceivedEvent) {
        if (!event.isWebhookMessage && event.author != event.jda.selfUser) {
            val serverContext = ServerContext(event.guild)
            if (containsCommand(serverContext, event.message)) {
                val args = commandArgs(event.message)
                val command = resolveCommand(args.first().substring(1))

                if (command != null) {
                    if (!event.author.isBot || command.allowBots) {
                        val mc = MessageContext.Builder().event(event).serverContext(serverContext).build()
                        if (mc.userCtx.allowed(command.level)) {
                            command.onCommand(mc, args.drop(1).toList())
                        } else {
                            mc.sendError("You are not allowed to run `%s`.").queue()
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    fun handleReady(event: ReadyEvent) {
        val jda = event.jda
        timer(name = "activityUpdate", daemon = true, period = 10000, action = {
            val count = jda.guildCache.map { it.memberCache.count() }.sum()
            jda.presence.game = Game.watching("%s users".format(count))
        })
    }
}