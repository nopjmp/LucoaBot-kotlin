package moe.giga.discord.util

enum class EventLogType {
    MEMBER_JOIN, MEMBER_LEAVE, ALL, UNKNOWN;

    override fun toString(): String {
        return when (this) {
            MEMBER_JOIN -> "member_join"
            MEMBER_LEAVE -> "member_leave"
            ALL -> "*"
            UNKNOWN -> "(unknown?)"
        }
    }

    companion object {
        fun fromString(str: String): EventLogType {
            return when (str) {
                "member_join" -> MEMBER_JOIN
                "member_leave" -> MEMBER_LEAVE
                "*" -> ALL
                else -> UNKNOWN
            }
        }
    }
}