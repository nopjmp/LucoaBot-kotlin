package moe.giga.discord

import moe.giga.discord.annotations.IsCommand
import moe.giga.discord.commands.Command
import net.dv8tion.jda.core.AccountType
import net.dv8tion.jda.core.JDABuilder
import net.dv8tion.jda.core.hooks.AnnotatedEventManager
import org.pmw.tinylog.Logger
import org.reflections.Reflections
import org.sqlite.SQLiteConfig
import java.io.File
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.util.*
import javax.security.auth.login.LoginException

object LucoaBot {
    //public static final int NORMAL_SHUTDOWN = 0;

    internal const val NEWLY_CREATED_CONFIG = 100

    private const val UNABLE_TO_CONNECT = 110
    private const val BAD_TOKEN = 111

    private const val PATH = "lucoa-bot.db"

    private lateinit var DSN: String
    private lateinit var config: SQLiteConfig

    val connection: Connection
        @Throws(SQLException::class)
        get() = DriverManager.getConnection(DSN, config.toProperties())

    @JvmStatic
    fun main(args: Array<String>) {
        if (System.getProperty("file.encoding") == "UTF-8") {
            val settings = SettingsManager.instance.settings

            DSN = "jdbc:sqlite:$PATH"
            val file = File(PATH)
            Logger.info("DB located: " + file.absolutePath)
            config = SQLiteConfig()
            config.setSharedCache(true)

            if (!file.exists()) {
                migrateDB()
            }

            setupBot(settings!!)
        } else {
            Logger.error("Please relaunch with file.encoding set to UTF-8")
        }
    }

    private fun migrateDB() {
        try {
            DriverManager.getConnection(DSN, config.toProperties()).use { connection ->
                val statement = connection.createStatement()
                statement.queryTimeout = 300

                /* All Discord IDs are 64bit unsigned integers, unfortunately SQLite doesn't properly support them.
             * So we use TEXT instead. BLOB would work here too, but not needed
             * */

                statement.executeUpdate("CREATE TABLE servers ( server_id TEXT PRIMARY KEY, prefix TEXT, log_channel TEXT, star_channel TEXT );")

                statement.executeUpdate("CREATE TABLE servers_roles( server_id TEXT, role_spec TEXT, role_id TEXT, UNIQUE(server_id, role_spec) ON CONFLICT REPLACE);")
                statement.executeUpdate("CREATE INDEX servers_roles_id ON servers_roles(server_id);")

                statement.executeUpdate("CREATE TABLE servers_self_roles(server_id TEXT, role_spec TEXT, role_id TEXT, UNIQUE(server_id, role_id) ON CONFLICT REPLACE);")
                statement.executeUpdate("CREATE INDEX servers_self_roles_id ON servers_self_roles(server_id);")

                statement.executeUpdate("CREATE TABLE custom_commands( server_id TEXT, command TEXT, response TEXT, UNIQUE(server_id, command) ON CONFLICT REPLACE);")
                statement.executeUpdate("CREATE INDEX custom_commands_id ON custom_commands(server_id);")

                statement.executeUpdate("CREATE TABLE servers_logs( server_id TEXT, event_name TEXT, channel_id TEXT );")
                statement.executeUpdate("CREATE INDEX servers_logs_id_name ON servers_logs(server_id, event_name);")
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }

    }

    private fun setupBot(settings: Settings) {
        try {
            val jdaBuilder = JDABuilder(AccountType.BOT).setToken(settings.botToken)

            jdaBuilder.setEventManager(AnnotatedEventManager())
            jdaBuilder.addEventListener(Handler(jdaBuilder, findCommands()))

            jdaBuilder.buildBlocking()
        } catch (e: LoginException) {
            e.printStackTrace()
            Logger.error("The bot token provided was most likely incorrect.")
            System.exit(BAD_TOKEN)
        } catch (e: InterruptedException) {
            e.printStackTrace()
            System.exit(UNABLE_TO_CONNECT)
        }

    }

    private fun findCommands(): List<Command> {
        val reflections = Reflections("moe.giga.discord.commands")
        val annotated = reflections.getTypesAnnotatedWith(IsCommand::class.java)

        val commands = ArrayList<Command>()
        for (clazz in annotated) {
            try {
                commands.add(clazz.getDeclaredConstructor().newInstance() as Command)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

        return commands
    }
}
