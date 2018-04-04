package moe.giga.discord;

import moe.giga.discord.annotations.CommandInfo;
import moe.giga.discord.commands.Command;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDABuilder;
import org.pmw.tinylog.Logger;
import org.sqlite.SQLiteConfig;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

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

            jdaBuilder.addEventListener(new CommandHandler(jdaBuilder, findCommands()));

            jdaBuilder.buildBlocking();
        } catch (LoginException e) {
            e.printStackTrace();
            Logger.error("The bot token provided was incorrect.");
            System.exit(BAD_TOKEN);
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.exit(UNABLE_TO_CONNECT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static List<Command> findCommands() throws IOException {
        List<Class<?>> packageClasses = getAllClassesInPackageContainingCommand();

        List<Command> commands = new ArrayList<>();

        for (Class<?> clazz : packageClasses) {
            try {
                CommandInfo commandInfo = clazz.getAnnotation(CommandInfo.class);
                if (commandInfo == null) continue;

                commands.add((Command) clazz.getDeclaredConstructor().newInstance());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return commands;
    }

    private static List<Class<?>> getAllClassesInPackageContainingCommand()
            throws IOException {
        String clazzPackageName = Command.class
                .getPackage()
                .getName();

        File clazzFile = new File(Command.class
                .getResource(".")
                .getFile());

        Path packagePath = Paths.get(clazzFile.getAbsolutePath());

        final List<Class<?>> packageClasses = new ArrayList<>();

        Files.walkFileTree(packagePath, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(
                    Path file, BasicFileAttributes attrs)
                    throws IOException {
                String filename =
                        file.getName(file.getNameCount() - 1).toString();

                if (filename.endsWith(".class")) {
                    String className = filename.replace(".class", "");

                    try {
                        Class<?> loadedClazz = Class.forName(
                                clazzPackageName + "." + className);

                        packageClasses.add(loadedClazz);
                    } catch (ClassNotFoundException e) {
                        System.err.println(
                                "class not found: " + e.getMessage());
                    }
                }

                return super.visitFile(file, attrs);
            }
        });

        return packageClasses;
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DSN, config.toProperties());
    }
}
