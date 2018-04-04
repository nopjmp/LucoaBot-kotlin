package moe.giga.discord;

import com.alibaba.fastjson.JSON;
import org.pmw.tinylog.Logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

public final class SettingsManager {
    private static SettingsManager instance;
    private Settings settings;
    private final Path configFile = new File(".").toPath().resolve("config.json");

    public static SettingsManager getInstance() {
        if (instance == null) {
            instance = new SettingsManager();
        }
        return instance;
    }

    private SettingsManager() {
        if (!configFile.toFile().exists()) {
            Logger.info("Creating Default Settings...");
            Logger.info("You will need to edit config.json with your login information.");
            this.settings = getDefaultSettings();
            saveSettings();
            System.exit(LucoaBot.NEWLY_CREATED_CONFIG);
        }
        loadSettings();
    }

    private Settings getDefaultSettings() {
        Settings newSettings = new Settings();

        newSettings.setBotToken("");

        return newSettings;
    }

    public void saveSettings() {
        try {
            BufferedWriter writer = Files.newBufferedWriter(configFile, StandardCharsets.UTF_8);
            JSON.writeJSONString(writer, this.settings);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadSettings() {
        try {
            BufferedReader reader = Files.newBufferedReader(configFile, StandardCharsets.UTF_8);
            String json = reader.lines().collect(Collectors.joining("\n"));
            this.settings = JSON.parseObject(json, Settings.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Settings getSettings() {
        return settings;
    }
}
