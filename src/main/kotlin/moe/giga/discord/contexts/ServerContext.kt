package moe.giga.discord.contexts

import moe.giga.discord.LucoaBot
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.Member
import net.dv8tion.jda.core.entities.User
import java.sql.SQLException
import java.util.*
import kotlin.collections.ArrayList

// log events structure?
class ServerContext(val guild: Guild) {
    var prefix: String = "."
        set(value) {
            if (value.isEmpty()) {
                throw IllegalArgumentException("Prefix was invalid!")
            }
            field = value
        }

    var starChannel: String? = ""

    var logChannel: String? = ""

    internal fun serverRoles(): Map<String, String>? {
        try {
            LucoaBot.connection.use { c ->
                val statement = c.prepareStatement("SELECT * FROM servers_roles WHERE server_id = ?")
                statement.setString(1, guild.id)

                val results = statement.executeQuery()
                val roles = HashMap<String, String>()
                while (results.next()) {
                    roles[results.getString("role_spec")] = results.getString("role_id")
                }

                return roles
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        return null
    }

    init {
        attachData()
    }

    private fun attachData() {
        try {
            LucoaBot.connection.use { c ->
                val statement = c.prepareStatement("SELECT * FROM servers WHERE server_id = ?")
                statement.setString(1, guild.id)

                val rs = statement.executeQuery()
                if (rs.next()) {
                    prefix = rs.getString("prefix")
                    logChannel = rs.getString("log_channel")
                    starChannel = rs.getString("star_channel")
                } else {
                    val createStatement = c.prepareStatement("insert into servers (server_id, prefix, log_channel, star_channel) values (?, \".\", null, null)")
                    with(createStatement) {
                        setString(1, guild.id)

                        executeUpdate()
                    }
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }

    internal fun getServerSelfRoles(): Map<String, List<String>> {
        try {
            LucoaBot.connection.use { c ->
                val statement = c.prepareStatement("SELECT * FROM servers_self_roles WHERE server_id = ?")
                statement.setString(1, guild.id)

                val results = statement.executeQuery()
                val roleList = ArrayList<Pair<String, String>>()
                while (results.next()) {
                    val roleSpec = results.getString("role_spec")
                    val roleId = results.getString("role_id")

                    roleList.add(Pair(roleSpec, roleId))
                }
                return roleList.groupBy({ it.first }, { it.second })
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }

        return mapOf()
    }

    fun save() {
        try {
            LucoaBot.connection.use { c ->
                val statement = c.prepareStatement("UPDATE servers SET prefix = ?, star_channel = ?, log_channel = ? WHERE server_id = ?")
                statement.setString(1, prefix)
                statement.setString(2, starChannel)
                statement.setString(3, logChannel)
                statement.setString(4, guild.id)

                statement.executeUpdate()
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }

    fun getMember(user: User): Member? = guild.getMember(user)
}
