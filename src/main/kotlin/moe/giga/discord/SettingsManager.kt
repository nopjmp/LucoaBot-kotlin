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

    private val envSettings: Settings
        get() {
            val newSettings = Settings()
            newSettings.botToken = dotenv["DISCORD_TOKEN"]
            newSettings.clientId = dotenv["DISCORD_CLIENT_ID"]
            newSettings.ownerId = dotenv["DISCORD_OWNER"]
            newSettings.dbPath = dotenv["DATA_PATH"]
            return newSettings
        }

    init {
        Logger.info("Loading settings from environment.")
        this.settings = envSettings
        if (this.settings.dbPath == null) {
            System.exit(LucoaBot.INVALID_SETTINGS)
        }
    }

    private object Holder {
        val INSTANCE = SettingsManager()
    }

    companion object {
        val instance: SettingsManager by lazy { Holder.INSTANCE }
    }
}
