package moe.giga.discord

// TODO: add proxy and other api keys here
data class Settings(
        val botToken: String? = null,
        val clientId: String? = null,
        val ownerId: String? = null,
        val datasource: String? = null
)
