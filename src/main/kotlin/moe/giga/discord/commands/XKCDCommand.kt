package moe.giga.discord.commands

import moe.giga.discord.contexts.MessageContext
import moe.giga.discord.util.HttpFetcher
import net.dv8tion.jda.core.EmbedBuilder
import java.io.IOException
import java.util.*

@Suppress("unused")
class XKCDCommand : Command {
    override val name = "xkcd"
    override val description = "Fetches xkcd comics and displays them. Maybe. Depends on the phase of the moon."
    override val usage = "xkcd [id | random]"

    private val httpFetcher = HttpFetcher(XKCDData::class.java)

    override fun execute(MC: MessageContext, args: List<String>) {
        val complete = { data: XKCDData ->
            MC.sendMessage(EmbedBuilder()
                    .setTitle("xkcd: ${data.safe_title}")
                    .setImage(data.img)
                    .setFooter(data.alt, null).build())
                    .queue()
        }
        val reportError = { _: IOException? -> MC.sendError("Error communicating with XKCD").queue() }
        try {
            when {
                args.isEmpty() -> {
                    httpFetcher.execute("https://xkcd.com/info.0.json", complete, reportError)
                }
                args.first().startsWith("rand") -> {
                    httpFetcher.execute("https://xkcd.com/info.0.json", { info ->
                        httpFetcher.execute("https://xkcd.com/${(0..info.num + 1).random()}/info.0.json",
                                complete, reportError)
                    }, reportError)
                }
                else -> {
                    val num = args.first().toInt()
                    httpFetcher.execute("https://xkcd.com/info.$num.json", complete, reportError)
                }
            }
        } catch (_: Exception) {
            reportError(null)
        }
    }

    data class XKCDData(
            val month: String,
            val num: Int,
            val link: String,
            val year: String,
            val news: String,
            val safe_title: String,
            val transcript: String,
            val alt: String,
            val img: String,
            val title: String,
            val day: String
    )

    companion object {
        fun ClosedRange<Int>.random() =
                Random().nextInt(endInclusive - start) + start
    }
}