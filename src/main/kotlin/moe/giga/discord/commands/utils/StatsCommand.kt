package moe.giga.discord.commands.utils

import moe.giga.discord.LucoaBot
import moe.giga.discord.commands.Command
import moe.giga.discord.contexts.MessageContext
import moe.giga.discord.util.username
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.JDABuilder
import java.io.IOException
import java.lang.management.ManagementFactory
import java.time.Duration
import java.util.*

@Suppress("unused")
class StatsCommand : Command {
    override val name = "stats"
    override val description = "Displays stats about the bot."

    private val properties = Properties()

    override fun init(builder: JDABuilder) {
        val loader = Thread.currentThread().contextClassLoader
        try {
            loader.getResourceAsStream("application.properties").use {
                this.properties.load(it)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    override fun execute(MC: MessageContext, args: List<String>) {
        val version = this.properties.getProperty("info.build.version", "(unknown)")
        val rb = ManagementFactory.getRuntimeMXBean()
        val heapMemoryUsage = ManagementFactory.getMemoryMXBean().heapMemoryUsage
        val bot = MC.jda.selfUser

        val uptime = Duration.ofMillis(rb.uptime)
        val uptimeStr = uptime.toString()
                .substring(2)
                .replace("(\\d[HMS])(?!$)".toRegex(), "$1 ")
                .toLowerCase()

        val memoryStr = String.format("%dMB / %dMB", heapMemoryUsage.used / (1024 * 1024),
                heapMemoryUsage.max / (1024 * 1024))

        val owner = MC.jda.asBot().applicationInfo.complete().owner

        val guilds = MC.jda.guildCache

        val memberCount = guilds.stream()
                .map { guild -> guild.memberCache.size() }
                .reduce(0, { a, b -> a!! + b!! })

        MC.sendMessage(EmbedBuilder().setColor(3447003)
                .setAuthor("LucoaBot $version", bot.avatarUrl)
                .addField("Owner", owner.username(), true)
                .addField("Uptime", uptimeStr, true)
                .addField("Bot ID", bot.id, true)
                .addField("Commands", LucoaBot.handler.commands.size.toString(), true)
                .addField("Messages", LucoaBot.statistics.messages.get().toString(), true)
                .addField("Commands Processed", LucoaBot.statistics.processedCommands.get().toString(), true)
                .addField("Memory Usage", memoryStr, true)
                .addField("Servers", guilds.size().toString(), true)
                .addField("Users", memberCount.toString(), true)
                .build()).queue()
    }
}