package moe.giga.discord;

import moe.giga.discord.annotations.CommandInfo;
import moe.giga.discord.commands.Command;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.hooks.AnnotatedEventManager;
import org.pmw.tinylog.Logger;
import org.reflections.Reflections;
import org.sqlite.SQLiteConfig;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class LucoaBot {
    //public static final int NORMAL_SHUTDOWN = 0;

    static final int NEWLY_CREATED_CONFIG = 100;

    private static final int UNABLE_TO_CONNECT = 110;
    private static final int BAD_TOKEN = 111;

    private static final String PATH = "lucoa-bot.db";

    private static String DSN;
    private static SQLiteConfig config;

    public static void main(String[] args) {
        if (System.getProperty("file.encoding").equals("UTF-8")) {
            DSN = "jdbc:sqlite:" + PATH;
            File file = new File(PATH);
            Logger.info("DB located: " + file.getAbsolutePath());
            if (!(file.exists())) {
                migrateDB();
            }

            config = new SQLiteConfig();
            config.setSharedCache(true);

            setupBot();
        } else {
            Logger.error("Please relaunch with file.encoding set to UTF-8");
        }
    }

    private static void migrateDB() {
        try (Connection connection = DriverManager.getConnection(DSN, config.toProperties())) {
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(300);

            /* All Discord IDs are 64bit unsigned integers, unfortunately SQLite doesn't properly support them.
             * So we use TEXT instead. BLOB would work here too, but not needed
             * */

            statement.executeUpdate("CREATE TABLE servers ( server_id TEXT PRIMARY KEY, prefix TEXT, log_channel TEXT, star_channel TEXT );");

            statement.executeUpdate("CREATE TABLE servers_roles( server_id TEXT, role_spec TEXT, role_id TEXT, UNIQUE(server_id, role_spec) ON CONFLICT REPLACE);");
            statement.executeUpdate("CREATE INDEX servers_roles_id ON servers_roles(server_id);");

            statement.executeUpdate("CREATE TABLE servers_self_roles(server_id TEXT, role_spec TEXT, role_id TEXT, UNIQUE(server_id, role_id) ON CONFLICT REPLACE);");
            statement.executeUpdate("CREATE INDEX servers_self_roles_id ON servers_self_roles(server_id);");

            statement.executeUpdate("CREATE TABLE custom_commands( server_id TEXT, command TEXT, response TEXT, UNIQUE(server_id, command) ON CONFLICT REPLACE);");
            statement.executeUpdate("CREATE INDEX custom_commands_id ON custom_commands(server_id);");

            statement.executeUpdate("CREATE TABLE servers_logs( server_id TEXT, event_name TEXT, channel_id TEXT );");
            statement.executeUpdate("CREATE INDEX servers_logs_id_name ON servers_logs(server_id, event_name);");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void setupBot() {
        try {
            Settings settings = SettingsManager.getInstance().getSettings();
            JDABuilder jdaBuilder = new JDABuilder(AccountType.BOT).setToken(settings.getBotToken());

            jdaBuilder.setEventManager(new AnnotatedEventManager());
            jdaBuilder.addEventListener(new CommandHandler(jdaBuilder, findCommands()));

            jdaBuilder.buildBlocking();
        } catch (LoginException e) {
            e.printStackTrace();
            Logger.error("The bot token provided was most likely incorrect.");
            System.exit(BAD_TOKEN);
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.exit(UNABLE_TO_CONNECT);
        }
    }

    private static List<Command> findCommands() {
        Reflections reflections = new Reflections("moe.giga.discord.commands");
        Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(CommandInfo.class);

        List<Command> commands = new ArrayList<>();
        for (Class<?> clazz : annotated) {
            try {
                commands.add((Command) clazz.getDeclaredConstructor().newInstance());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return commands;
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DSN, config.toProperties());
    }
}
