package moe.giga.discord

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.github.cdimascio.dotenv.dotenv
import kotliquery.Session
import kotliquery.sessionOf


object DB {

    private val config = HikariConfig()
    private var ds: HikariDataSource? = null

    val session: Session
        get() = sessionOf(ds!!)

    init {
        val dotenv = dotenv {
            directory = "./"
            ignoreIfMalformed = true
            ignoreIfMissing = true
        }
        config.jdbcUrl = dotenv["DATASOURCE"]
        config.connectionTimeout = 1000
        ds = HikariDataSource(config)
    }
}