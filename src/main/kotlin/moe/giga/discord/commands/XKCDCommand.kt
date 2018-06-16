package moe.giga.discord.commands

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import moe.giga.discord.contexts.MessageContext
import net.dv8tion.jda.core.EmbedBuilder
import okhttp3.*
import java.io.IOException
import java.util.*

@Suppress("unused")
class XKCDCommand : Command {
    override val name = "xkcd"
    override val description = "Fetches xkcd comics and displays them. Maybe. Depends on the phase of the moon."
    override val usage = "xkcd [id | random]"

    private val client = OkHttpClient()

    private val dataJsonAdapter = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
            .adapter(XKCDData::class.java)

    private fun fetchData(url: String, action: (Response) -> Unit, error: (IOException?) -> Unit) {
        val request = Request.Builder()
                .url(url)
                .build()

        val func = { r: Response? ->
            if (r == null || !r.isSuccessful)
                throw IOException("Unexpected response")
            action(r)
        }

        client.newCall(request).enqueue(OkHttpCallbackProxy(func, error))
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
                        success(dataJsonAdapter.fromJson(it.body()!!.source())
                                ?: throw IOException("Unexpected response"))
                    }, errorAction)
                }
                args.first().startsWith("rand") -> {
                    fetchData("https://xkcd.com/info.0.json", {
                        val info: XKCDData = dataJsonAdapter.fromJson(it.body()!!.source())
                                ?: throw IOException("Unexpected response")

                        fetchData("https://xkcd.com/${(0..info.num + 1).random()}/info.0.json", {
                            success(dataJsonAdapter.fromJson(it.body()!!.source())
                                    ?: throw IOException("Unexpected response"))
                        }, errorAction)
                    }, errorAction)
                }
                else -> {
                    val num = args.first().toInt()
                    fetchData("https://xkcd.com/info.$num.json", {
                        success(dataJsonAdapter.fromJson(it.body()!!.source())
                                ?: throw IOException("Unexpected response"))
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