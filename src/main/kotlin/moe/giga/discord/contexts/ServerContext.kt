package moe.giga.discord.contexts

import kotliquery.Row
import kotliquery.queryOf
import kotliquery.using
import moe.giga.discord.DB
import moe.giga.discord.Handler
import moe.giga.discord.util.AccessLevel
import moe.giga.discord.util.EventLogType
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.Member
import net.dv8tion.jda.core.entities.User
import kotlin.reflect.KProperty

// log events structure?
class ServerContext(val guild: Guild) {
    val guildId: Long
        get() = guild.idLong

    inner class DatabaseProp<T>(columnName: String, mapFunc: (Row) -> T, private val default: T) {
        private val getSQL = queryOf("SELECT $columnName FROM servers WHERE server_id = ?", guildId)
                .map(mapFunc).asSingle
        private val setSQL = "UPDATE servers SET $columnName = ? WHERE server_id = ?"

        operator fun getValue(thisRef: Any?, p: KProperty<*>): T {
            return using(DB.session) { session ->
                session.run(getSQL)
            } ?: default
        }

        operator fun setValue(thisRef: Any?, p: KProperty<*>, v: T) {
            using(DB.session) { session ->
                session.run(queryOf(setSQL, v, guildId).asUpdate)
            }
        }
    }

    var prefix by DatabaseProp("prefix", { it.string("prefix") }, ".")
    var starChannel by DatabaseProp("star_channel", { it.longOrNull("star_channel") }, null)

    companion object {
        const val FETCH_SERVER_ROLES = "SELECT role_spec, role_id FROM servers_roles WHERE server_id = ?"
        const val FETCH_SERVER = "SELECT server_id FROM servers WHERE server_id = ?"
        const val INSERT_SERVER = "INSERT INTO servers (server_id, prefix, log_channel, star_channel) VALUES (?, ?, null, null)"
        const val INSERT_ROLE_SPEC = "INSERT INTO servers_roles(server_id, role_spec, role_id) VALUES (?, ?, ?) " +
                "ON CONFLICT (server_id, role_spec) DO UPDATE " +
                "SET role_id = excluded.role_id"
        const val INSERT_SELF_ROLE = "INSERT INTO servers_self_roles(server_id, role_spec, role_id) VALUES (?, ?, ?)"
        const val DELETE_SELF_ROLE = "DELETE FROM servers_self_roles WHERE server_id = ? AND role_id = ?"
        const val FETCH_SELF_ROLES = "SELECT * FROM servers_self_roles WHERE server_id = ?"
        const val DELETE_EVENT_LOG = "DELETE FROM servers_logs WHERE server_id = ? AND channel_id = ?"
        const val FETCH_STAR_EVENT_LOG = "SELECT channel_id FROM servers_logs WHERE server_id = ? AND event_name = '*'"
        const val UPDATE_EVENT_LOG = "INSERT INTO servers_logs (server_id, event_name, channel_id) VALUES (?, ?, ?)"
        const val FETCH_EVENT_LOG = "SELECT channel_id FROM servers_logs WHERE server_id = ? AND (event_name = ? OR event_name = '*')"
        const val FIND_CUSTOM_COMMAND = "SELECT response FROM custom_commands WHERE server_id = ? AND command = ?"
    }

    private fun serverRoles(): Map<String, Long> {
        val selectQuery = queryOf(FETCH_SERVER_ROLES, guildId)
                .map { Pair(it.string("role_spec"), it.long("role_id")) }.asList
        return using(DB.session) { session ->
            session.run(selectQuery).associate { it }
        }
    }

    init {
        using(DB.session) { session ->
            if (session.run(queryOf(FETCH_SERVER, guildId)
                            .map { it.longOrNull("server_id") }.asSingle) == null) {
                session.run(queryOf(INSERT_SERVER, guildId, Handler.DEFAULT_PREFIX).asUpdate)
            }
        }
    }

    internal fun addSpecRole(spec: String, id: Long) {
        using(DB.session) { session ->
            session.run(queryOf(INSERT_ROLE_SPEC, guildId, spec, id).asUpdate)
        }
    }

    internal fun addSelfRole(group: String, id: Long) {
        using(DB.session) { session ->
            session.run(queryOf(INSERT_SELF_ROLE, guildId, group, id).asUpdate)
        }
    }

    internal fun deleteSelfRole(id: Long) {
        using(DB.session) { session ->
            session.run(queryOf(DELETE_SELF_ROLE, guildId, id).asUpdate)
        }
    }


    internal fun getServerSelfRoles(): Map<String, List<Long>> {
        return using(DB.session) { session ->
            session.run(queryOf(FETCH_SELF_ROLES, guildId)
                    .map { Pair(it.string("role_spec"), it.long("role_id")) }.asList)
                    .groupBy({ it.first }, { it.second })
        }
    }

    internal fun getMember(user: User): Member? = guild.getMember(user)
    internal fun resolvePermissions(user: User): AccessLevel {
        val roles = serverRoles().mapValues { guild.getRoleById(it.value) }
        val member = getMember(user)
        if (member != null) {
            if (roles["admin"] != null) {
                if (member.roles.contains(roles["admin"])) {
                    return AccessLevel.ADMIN
                }
            }

            if (roles["mod"] != null) {
                if (member.roles.contains(roles["mod"])) {
                    return AccessLevel.MOD
                }
            }
        }
        return AccessLevel.USER
    }

    internal fun deleteEventLog(channel: Long) {
        using(DB.session) { session ->
            session.run(queryOf(DELETE_EVENT_LOG, guildId, channel).asUpdate)
        }
    }

    internal fun setEventLog(eventLogType: EventLogType, channel: Long): Boolean {
        return using(DB.session) { session ->
            val list = session.run(queryOf(FETCH_STAR_EVENT_LOG, guildId).map { it.long("channel_id") }.asList)
            if (list.contains(channel)) {
                return@using 0
            }

            session.run(queryOf(UPDATE_EVENT_LOG, guildId, eventLogType.toString(), channel).asUpdate)
        } > 0
    }

    internal fun logEvent(eventLogType: EventLogType): List<Long> {
        return using(DB.session) { session ->
            session.run(queryOf(FETCH_EVENT_LOG, guildId, eventLogType.toString()).map { it.long("channel_id") }.asList)
        }
    }

    internal fun findCustomCommand(name: String): String? {
        return using(DB.session) { session ->
            session.run(queryOf(FIND_CUSTOM_COMMAND, guildId, name).map { it.string("response") }.asSingle)
        }
    }
}
