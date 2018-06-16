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

        val func = { r: Response? ->
            if (r == null || !r.isSuccessful)
                throw IOException("Unexpected response")
            val data = dataJsonAdapter.fromJson(r.body()!!.source()) ?: throw IOException("Unexpected response")
            action(data)
        }

        client.newCall(request).enqueue(OkHttpCallbackProxy(func, error))
    }

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
}