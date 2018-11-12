package moe.giga.discord.util

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.*
import java.io.IOException

class HttpFetcher<T>(clazz: Class<T>) {
    private val client = OkHttpClient()

    private val dataJsonAdapter = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
            .adapter(clazz)

    fun execute(url: String, action: (T) -> Unit, error: (IOException?) -> Unit) {
        val request = Request.Builder()
                .url(url)
                .build()

        client.newCall(request).enqueue(OkHttpCallbackProxy(action, error))
    }

    inner class OkHttpCallbackProxy(private val success: (T) -> Unit,
                                    private val error: (IOException?) -> Unit) : Callback {
        override fun onFailure(call: Call?, e: IOException?) {
            error(e)
        }

        override fun onResponse(call: Call?, resp: Response?) {
            when {
                resp == null || !resp.isSuccessful -> error(IOException("Unexpected response"))
                resp.code() != 200 -> {
                    error(IOException("Unexpected status code"))
                }
                else -> resp.body()?.use { body ->
                    success(dataJsonAdapter.fromJson(body.source())
                            ?: throw IOException("Unexpected response"))
                }
            }
        }
    }
}