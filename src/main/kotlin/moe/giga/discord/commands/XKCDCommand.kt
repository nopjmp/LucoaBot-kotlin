package moe.giga.discord.commands

import com.google.gson.Gson
import moe.giga.discord.annotations.IsCommand
import moe.giga.discord.contexts.MessageContext
import net.dv8tion.jda.core.EmbedBuilder
import okhttp3.*
import java.io.IOException
import java.util.*

@IsCommand()
class XKCDCommand : Command() {
    override val name = "xkcd"
    override val description = "Fetches xkcd comics and displays them. Maybe. Depends on the phase of the moon."
    override val usage = "xkcd [id | random]"

    private val client = OkHttpClient()

    private fun fetchData(url: String, action: (Response?) -> Unit, error: (IOException?) -> Unit) {
        val request = Request.Builder()
                .url(url)
                .build()

        client.newCall(request).enqueue(OkHttpCallbackProxy(action, error))
    }

    override fun execute(MC: MessageContext, args: List<String>) {
        val success = { data: XKCDData ->
            MC.sendMessage(EmbedBuilder()
                    .setTitle("xkcd: ${data.safe_title}")
                    .setImage(data.img)
                    .setFooter(data.alt, null).build())
                    .queue()
        }
        val errorAction = { _: IOException? -> MC.sendError("Error communicating with XKCD").queue() }
        try {
            when {
                args.isEmpty() -> {
                    fetchData("https://xkcd.com/info.0.json", {
                        success(Gson().fromJson(it?.body()?.charStream(), XKCDData::class.java))
                    }, errorAction)
                }
                args.first().startsWith("rand") -> {
                    fetchData("https://xkcd.com/info.0.json", {
                        val info: XKCDData = Gson().fromJson(it?.body()?.charStream(), XKCDData::class.java)

                        fetchData("https://xkcd.com/${(0..info.num + 1).random()}/info.0.json", {
                            success(Gson().fromJson(it?.body()?.charStream(), XKCDData::class.java))
                        }, errorAction)
                    }, errorAction)
                }
                else -> {
                    val num = args.first().toInt()
                    fetchData("https://xkcd.com/info.$num.json", {
                        success(Gson().fromJson(it?.body()?.charStream(), XKCDData::class.java))
                    }, errorAction)
                }
            }
        } catch (_: Exception) {
            errorAction(null)
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

    private class OkHttpCallbackProxy(private val success: (Response?) -> Unit, private val error: (IOException?) -> Unit) : Callback {
        override fun onFailure(call: Call?, e: IOException?) {
            error(e)
        }

        override fun onResponse(call: Call?, response: Response?) {
            if (response?.code() != 200) {
                error(null)
            } else {
                success(response)
            }
        }
    }

    companion object {
        fun ClosedRange<Int>.random() =
                Random().nextInt(endInclusive - start) + start
    }
}