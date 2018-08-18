package moe.giga.discord

import io.github.cdimascio.dotenv.dotenv
import org.pmw.tinylog.Logger

class SettingsManager private constructor() {
    var settings: Settings
        private set

    private val dotenv = dotenv {
        directory = "./"
        ignoreIfMalformed = true
        ignoreIfMissing = true
    }

    private val envSettings = Settings(
            dotenv["DISCORD_TOKEN"],
            dotenv["DISCORD_CLIENT_ID"],
            dotenv["DISCORD_OWNER"]
    )

    init {
        Logger.info("Loading settings from environment.")
        this.settings = envSettings
    }

    private object Holder {
        val INSTANCE = SettingsManager()
    }

    companion object {
        val instance: SettingsManager by lazy { Holder.INSTANCE }
    }
}
