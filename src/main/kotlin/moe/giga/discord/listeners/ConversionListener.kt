package moe.giga.discord.listeners

import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.hooks.SubscribeEvent
import java.text.DecimalFormat

@BotListener
class ConversionListener {
    private val findRegex = Regex("""(?<=^|\s|[_*~])(-?\d*(?:\.\d+)?)\s?°?([FC])(?=$|\s|[_*~])""", RegexOption.IGNORE_CASE)

    data class UnitPair(val quantity: Double, val unit: String)

    @SubscribeEvent
    fun onMessage(event: MessageReceivedEvent) {
        if (event.author.isBot) return
        try {
            val results = findRegex.findAll(event.message.contentRaw)
            if (results.any()) {
                val textList = mutableListOf<String>()
                val pairs = results.map { it.groupValues }.filter { it.size == 3 }
                        .map { UnitPair(it[1].toDouble(), it[2].toUpperCase()) }
                        .distinct()
                        .take(5)

                for (value in pairs) {
                    when (value.unit) {
                        "C" -> textList.add("${value.quantity.format(2)} °C = " +
                                "${(value.quantity * 1.8 + 32).format(2)} °F")
                        "F" -> textList.add("${value.quantity.format(2)} °F = " +
                                "${((value.quantity - 32) / 1.8).format(2)} °C")
                    }
                }

                if (textList.any()) event.channel.sendMessage(textList.joinToString("\n")).queue()
            }
        } catch (_: Exception) {
            // do nothing
        }
    }
}

fun Double.format(digits: Int): String {
    val df = DecimalFormat()
    df.maximumFractionDigits = digits
    return df.format(this)
}