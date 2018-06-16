package moe.giga.discord.commands

import moe.giga.discord.contexts.MessageContext
import moe.giga.discord.contexts.ServerContext
import moe.giga.discord.contexts.UserContext
import moe.giga.discord.util.AccessLevel
import moe.giga.discord.util.EventLogType
import net.dv8tion.jda.core.JDABuilder
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent
import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent
import net.dv8tion.jda.core.hooks.SubscribeEvent

@Suppress("unused")
class EventLogCommand : Command {
    override val name = "eventlog"
    override val hidden = true
    override val level = AccessLevel.MOD

    //private val argRegex = Regex("""<#(\d+)>""")

    override fun init(builder: JDABuilder) {
        builder.addEventListener(this)
    }

    override fun execute(MC: MessageContext, args: List<String>) {
        if (args.isNotEmpty()) {
            if (args[0].equals("none", ignoreCase = true)) {
                MC.serverCtx.deleteEventLog(MC.channel.id)
            }
        } else {
            MC.serverCtx.setEventLog(EventLogType.ALL, MC.channel.id)
            MC.sendMessage("Event log set to ${MC.channel.name}").queue()
            return
        }
        MC.sendError("args invalid?").queue()
    }

    // TODO: make embed version?
    private fun notify(serverContext: ServerContext, channelIds: List<String>, message: String) {
        val channels = channelIds.map { serverContext.guild!!.getTextChannelById(it) }
                .filter { it != null }
        channels.forEach { it.sendMessage(message).queue() }
    }

//    private fun notify(serverContext: ServerContext, channelIds: List<String>, message: MessageEmbed) {
//        val channels = channelIds.map { serverContext.guild.getTextChannelById(it) }
//                .filter { it != null }
//        channels.forEach { it.sendMessage(message).queue() }
//    }

    @SubscribeEvent
    fun memberJoin(event: GuildMemberJoinEvent) {
        val serverContext = ServerContext(event.guild)
        val userContext = UserContext(event.user, serverContext)
        val channelList = serverContext.logEvent(EventLogType.MEMBER_JOIN)
        if (channelList.isNotEmpty())
            notify(serverContext, channelList, "${userContext.asText} has joined.")
    }

    @SubscribeEvent
    fun memberLeave(event: GuildMemberLeaveEvent) {
        val serverContext = ServerContext(event.guild)
        val userContext = UserContext(event.user, serverContext)
        val channelList = serverContext.logEvent(EventLogType.MEMBER_LEAVE)
        if (channelList.isNotEmpty())
            notify(serverContext, channelList, "${userContext.asText} has left.")
    }
}
