package moe.giga.discord

import moe.giga.discord.commands.custom.AddCustomCommand
import moe.giga.discord.commands.custom.RemoveCustomCommand
import moe.giga.discord.contexts.ServerContext
import org.pmw.tinylog.Logger
import org.sqlite.SQLiteConfig
import java.io.File
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.SQLException

object Database {
    internal val connection: Connection
    private const val DB_NAME = "lucoa-data.db"

    private val preparedStatements: HashMap<String, PreparedStatement>

    init {
        val settings = SettingsManager.instance.settings
        val path = "${settings.dbPath}/$DB_NAME"

        val file = File(path)
        Logger.info("DB located: " + file.absolutePath)
        val config = SQLiteConfig()
        config.setSharedCache(true)
        val migrate = !file.exists()

        connection = DriverManager.getConnection("jdbc:sqlite:$path", config.toProperties())

        if (migrate) {
            migrateDB()
        }

        preparedStatements = hashMapOf(
                ServerContext.FETCH_SERVER_ROLES to connection.prepareStatement("SELECT * FROM servers_roles WHERE server_id = ?"),
                ServerContext.FETCH_SERVER to connection.prepareStatement("SELECT * FROM servers WHERE server_id = ?"),
                ServerContext.INSERT_SERVER to connection.prepareStatement("INSERT INTO servers (server_id, prefix, log_channel, star_channel) VALUES (?, \".\", null, null)"),
                ServerContext.INSERT_ROLE_SPEC to connection.prepareStatement("INSERT OR REPLACE INTO servers_roles(server_id, role_spec, role_id) VALUES (?, ?, ?)"),
                ServerContext.INSERT_SELF_ROLE to connection.prepareStatement("INSERT OR REPLACE INTO servers_self_roles(server_id, role_spec, role_id) VALUES (?, ?, ?)"),
                ServerContext.DELETE_SELF_ROLE to connection.prepareStatement("DELETE FROM servers_self_roles WHERE server_id = ? AND role_id = ?"),
                ServerContext.FETCH_SELF_ROLES to connection.prepareStatement("SELECT * FROM servers_self_roles WHERE server_id = ?"),
                ServerContext.SAVE_SERVER to connection.prepareStatement("UPDATE servers SET prefix = ?, star_channel = ?, log_channel = ? WHERE server_id = ?"),
                ServerContext.DELETE_EVENT_LOG to connection.prepareStatement("DELETE FROM servers_logs WHERE server_id = ? AND channel_id = ?"),
                ServerContext.FETCH_STAR_EVENT_LOG to connection.prepareStatement("SELECT channel_id FROM servers_logs WHERE server_id = ? AND event_name = '*'"),
                ServerContext.UPDATE_EVENT_LOG to connection.prepareStatement("INSERT INTO servers_logs (server_id, event_name, channel_id) VALUES (?, ?, ?)"),
                ServerContext.FETCH_EVENT_LOG to connection.prepareStatement("SELECT channel_id FROM servers_logs WHERE server_id = ? AND (event_name = ? OR event_name = '*')"),
                ServerContext.FETCH_CUSTOM_COMMANDS to connection.prepareStatement("SELECT command, response FROM custom_commands WHERE server_id = ?"),
                ServerContext.FIND_CUSTOM_COMMAND to connection.prepareStatement("SELECT response FROM custom_commands WHERE server_id = ? AND command = ?"),
                AddCustomCommand.ADD_CUSTOM_COMMAND to connection.prepareStatement("INSERT INTO custom_commands (server_id, command, response) VALUES (?, ?, ?)"),
                RemoveCustomCommand.DELETE_CUSTOM_COMMAND to connection.prepareStatement("DELETE FROM custom_commands WHERE server_id = ? AND command = ?")
        )
    }

    private fun migrateDB() {
        try {
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
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }

    private fun statement(statementName: String) = when {
        preparedStatements.containsKey(statementName) -> preparedStatements[statementName]!!
        else -> throw RuntimeException("$statementName does not exist!!!!")
    }

    internal inline fun <R> withStatement(statementName: String, body: PreparedStatement.() -> R): R {
        val statement = statement(statementName)
        try {
            return body(statement)
        } finally {
            statement.clearParameters()
        }
    }
}