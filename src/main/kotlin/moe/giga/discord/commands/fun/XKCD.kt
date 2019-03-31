package moe.giga.discord.commands.`fun`

import moe.giga.discord.commands.Command
import moe.giga.discord.contexts.MessageContext
import moe.giga.discord.repositories.XKCDApi
import net.dv8tion.jda.core.EmbedBuilder
import java.util.*

@Suppress("unused")
class XKCD : Command {
    override val name = "xkcd"
    override val description = "Fetches xkcd comics and displays them. Maybe. Depends on the phase of the moon."
    override val usage = "xkcd [id | random]"

    private val xkcd = XKCDApi.create()

    override fun execute(MC: MessageContext, args: List<String>) {
        try {
            val data = when {
                args.isEmpty() -> xkcd.latest().execute().body()!!
                args.first().startsWith("rand") -> {
                    val info = xkcd.latest().execute().body()!!
                    xkcd.num((0..info.num + 1).random()).execute().body()!!
                }
                else -> xkcd.num(args.first().toInt()).execute().body()!!
            }
            MC.sendMessage(EmbedBuilder()
                    .setTitle("xkcd: ${data.safe_title}")
                    .setImage(data.img)
                    .setFooter(data.alt, null).build())
                    .queue()
        } catch (e: Exception) {
            e.printStackTrace()
            MC.sendError("Error communicating with XKCD").queue()
        }
    }

    companion object {
        fun ClosedRange<Int>.random() =
                Random().nextInt(endInclusive - start) + start
    }
}