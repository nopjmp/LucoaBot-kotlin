package moe.giga.discord.repositories

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

interface XKCDApi {

    @GET("/info.0.json")
    fun latest(): Call<Data>

    @GET("/{num}/info.0.json")
    fun num(@Path("num") num: Int): Call<Data>

    companion object {
        fun create(): XKCDApi {
            val retrofit = Retrofit.Builder()
                    //.addCallAdapterFactory(CoroutineCallAdapterFactory())
                    .addConverterFactory(MoshiConverterFactory.create())
                    .baseUrl("https://xkcd.com/")
                    .build()
            return retrofit.create(XKCDApi::class.java)
        }
    }

    data class Data(
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
}