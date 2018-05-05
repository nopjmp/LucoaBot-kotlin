package moe.giga.discord

import com.alibaba.fastjson.JSON
import org.pmw.tinylog.Logger
import java.io.File
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.util.stream.Collectors

class SettingsManager private constructor() {
    lateinit var settings: Settings
        private set

    private val configFile = File(".").toPath().resolve("config.json")

    private val defaultSettings: Settings
        get() {
            val newSettings = Settings()
            newSettings.botToken = ""
            newSettings.ownerId = ""
            return newSettings
        }

    init {
        if (!configFile.toFile().exists()) {
            Logger.info("Creating Default Settings...")
            Logger.info("You will need to edit config.json with your login information.")
            this.settings = defaultSettings
            saveSettings()
            System.exit(LucoaBot.NEWLY_CREATED_CONFIG)
        }
        loadSettings()
    }

    private fun saveSettings() {
        try {
            val writer = Files.newBufferedWriter(configFile, StandardCharsets.UTF_8)
            JSON.writeJSONString(writer, this.settings)
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    private fun loadSettings() {
        try {
            val reader = Files.newBufferedReader(configFile, StandardCharsets.UTF_8)
            val json = reader.lines().collect(Collectors.joining("\n"))
            this.settings = JSON.parseObject(json, Settings::class.java)
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    private object Holder {
        val INSTANCE = SettingsManager()
    }

    companion object {
        val instance: SettingsManager by lazy { Holder.INSTANCE }
    }
}
