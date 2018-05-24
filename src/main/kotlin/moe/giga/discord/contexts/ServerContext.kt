package moe.giga.discord.contexts

import moe.giga.discord.util.AccessLevel
import moe.giga.discord.util.Database
import moe.giga.discord.util.EventLogType
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.Member
import net.dv8tion.jda.core.entities.User
import java.sql.SQLException

// log events structure?
class ServerContext(val guild: Guild?) {
    var prefix: String = "."
    var starChannel: String? = ""
    var logChannel: String? = ""

    companion object {
        const val FETCH_SERVER_ROLES = "serverRolesOp"
        const val FETCH_SERVER = "serverOp"
        const val FETCH_SELF_ROLES = "serverSelfRolesOp"
        const val INSERT_SERVER = "serverAddOp"
        const val INSERT_ROLE_SPEC = "serverRoleSpecAddOp"
        const val INSERT_SELF_ROLE = "serverSelfRoleAddOp"
        const val DELETE_SELF_ROLE = "serverSelfRoleDeleteOp"
        const val SAVE_SERVER = "serverSaveOp"
        const val FETCH_STAR_EVENT_LOG = "serverEventLogFetchStarOp"
        const val FETCH_EVENT_LOG = "serverEventLogFetchOp"
        const val DELETE_EVENT_LOG = "serverEventLogDeleteOp"
        const val UPDATE_EVENT_LOG = "serverEventLogUpdateOp"
        const val FETCH_CUSTOM_COMMANDS = "serverCustomCommandsOp"
        const val FIND_CUSTOM_COMMAND = "serverCustomCommandsFindOp"
    }

    private fun serverRoles(): Map<String, String> {
        if (guild != null) {
            try {
                return Database.withStatement(FETCH_SERVER_ROLES) {
                    setString(1, guild.id)
                    val results = executeQuery()
                    val roles = HashMap<String, String>()
                    while (results.next()) {
                        roles[results.getString("role_spec")] = results.getString("role_id")
                    }

                    roles
                }
            } catch (e: SQLException) {
                e.printStackTrace()
            }
        }
        return mapOf()
    }

    init {
        attachData()
    }

    private fun attachData() {
        if (guild != null) {
            try {
                Database.withStatement(FETCH_SERVER) {
                    setString(1, guild.id)

                    val rs = executeQuery()
                    if (rs.next()) {
                        prefix = rs.getString("prefix")
                        logChannel = rs.getString("log_channel")
                        starChannel = rs.getString("star_channel")
                    } else {
                        Database.withStatement(INSERT_SERVER) {
                            setString(1, guild.id)

                            executeUpdate()
                        }
                    }
                }
            } catch (e: SQLException) {
                e.printStackTrace()
            }
        }
    }

    internal fun addSpecRole(spec: String, id: String) {
        if (guild != null) {
            try {
                Database.withStatement(INSERT_ROLE_SPEC) {
                    setString(1, guild.id)
                    setString(2, spec)
                    setString(3, id)

                    executeUpdate()
                }
            } catch (e: SQLException) {
                e.printStackTrace()
            }
        }
    }

    internal fun addSelfRole(group: String, id: String) {
        if (guild != null) {
            try {
                Database.withStatement(INSERT_SELF_ROLE) {
                    setString(1, guild.id)
                    setString(2, group)
                    setString(3, id)
                    executeUpdate()
                }
            } catch (e: SQLException) {
                e.printStackTrace()
            }
        }
    }

    internal fun deleteSelfRole(id: String) {
        if (guild != null) {
            try {
                Database.withStatement(DELETE_SELF_ROLE) {
                    setString(1, guild.id)
                    setString(2, id)
                    executeUpdate()
                }
            } catch (e: SQLException) {
                e.printStackTrace()
            }
        }
    }


    internal fun getServerSelfRoles(): Map<String, List<String>> {
        if (guild != null) {
            try {
                return Database.withStatement(FETCH_SELF_ROLES) {
                    setString(1, guild.id)

                    val results = executeQuery()
                    val roleList = ArrayList<Pair<String, String>>()
                    while (results.next()) {
                        val roleSpec = results.getString("role_spec")
                        val roleId = results.getString("role_id")

                        roleList.add(Pair(roleSpec, roleId))
                    }
                    roleList.groupBy({ it.first }, { it.second })
                }
            } catch (e: SQLException) {
                e.printStackTrace()
            }
        }

        return mapOf()
    }

    fun save() {
        if (guild != null) {
            try {
                Database.withStatement(SAVE_SERVER) {
                    setString(1, prefix)
                    setString(2, starChannel)
                    setString(3, logChannel)
                    setString(4, guild.id)
                    executeUpdate()
                }
            } catch (e: SQLException) {
                e.printStackTrace()
            }
        }
    }

    internal fun getMember(user: User): Member? = guild?.getMember(user)
    internal fun resolvePermissions(user: User): AccessLevel {
        if (guild != null) {
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
        }
        return AccessLevel.USER
    }

    internal fun deleteEventLog(channel: String) {
        if (guild != null) {
            try {
                Database.withStatement(DELETE_EVENT_LOG) {
                    setString(1, guild.id)
                    setString(2, channel)
                    executeUpdate()
                }
            } catch (e: SQLException) {
                e.printStackTrace()
            }
        }
    }

    internal fun setEventLog(eventLogType: EventLogType, channel: String): Boolean {
        if (guild != null) {
            try {
                Database.withStatement(FETCH_STAR_EVENT_LOG) {
                    val checkResult = executeQuery()
                    if (checkResult.next()) {
                        val allChannel = checkResult.getString("channel_id")
                        if (allChannel == channel) {
                            return false
                        }
                    }
                }

                return Database.withStatement(UPDATE_EVENT_LOG) {
                    setString(1, guild.id)
                    setString(2, eventLogType.toString())
                    setString(3, channel)
                    executeUpdate() > 0
                }
            } catch (e: SQLException) {
                e.printStackTrace()
            }
        }
        return false
    }

    internal fun logEvent(eventLogType: EventLogType): List<String> {
        if (guild != null) {
            try {
                return Database.withStatement(FETCH_EVENT_LOG) {
                    setString(1, guild.id)
                    setString(2, eventLogType.toString())

                    val results = executeQuery()
                    val list = ArrayList<String>()
                    while (results.next())
                        list.add(results.getString("channel_id"))
                    list
                }
            } catch (e: SQLException) {
                e.printStackTrace()
            }
        }
        return listOf()
    }

    internal fun findCustomCommand(name: String): String? {
        if (guild != null) {
            try {
                Database.withStatement(FIND_CUSTOM_COMMAND) {
                    setString(1, guild.id)
                    setString(2, name)

                    val results = executeQuery()
                    if (results.next())
                        return results.getString("response")
                }
            } catch (e: SQLException) {
                e.printStackTrace()
            }
        }
        return null
    }

//    internal val customCommands by lazy {
//        if (guild != null) {
//            try {
//                return@lazy Database.withStatement(FETCH_CUSTOM_COMMANDS) {
//                    setString(1, guild.id)
//
//                    val results = executeQuery()
//                    val map = HashMap<String, String>()
//                    while (results.next())
//                        map[results.getString("command")] = results.getString("response")
//                    map
//                }
//            } catch (e: SQLException) {
//                e.printStackTrace()
//            }
//        }
//        hashMapOf<String, String>()
//    }
}
