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

    class IOExceptionWrapper(private val e: Exception) : IOException(e.message) {
        override fun getStackTrace(): Array<StackTraceElement> {
            return e.stackTrace
        }

        override val cause: Throwable?
            get() = e.cause

        override val message: String?
            get() = e.message

        override fun equals(other: Any?): Boolean {
            return e == other
        }

        override fun hashCode(): Int {
            return e.hashCode()
        }
    }

    inner class OkHttpCallbackProxy(private val success: (T) -> Unit,
                                    private val error: (IOException?) -> Unit) : Callback {
        override fun onFailure(call: Call?, e: IOException?) {
            error(e)
        }

        override fun onResponse(call: Call?, resp: Response?) {
            try {
                if (resp == null || !resp.isSuccessful)
                    throw IOException("Unexpected response")
                if (resp.code() != 200)
                    throw IOException("Unexpected status code")

                success(dataJsonAdapter.fromJson(resp.body()!!.source()) ?: throw IOException("Unexpected response"))
            } catch (e: IOException) {
                error(e)
            } catch (e: Exception) {
                error(IOExceptionWrapper(e))
            }
        }
    }
}