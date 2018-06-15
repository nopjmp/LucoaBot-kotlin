package moe.giga.discord.util

enum class EventLogType {
    RED_ALERT, MEMBER_JOIN, MEMBER_LEAVE, ALL, UNKNOWN;

    override fun toString(): String {
        return when (this) {
            RED_ALERT -> "red_alert"
            MEMBER_JOIN -> "member_join"
            MEMBER_LEAVE -> "member_leave"
            ALL -> "*"
            UNKNOWN -> "(unknown?)"
        }
    }

    companion object {
        fun fromString(str: String): EventLogType {
            return when (str) {
                "red_alert" -> RED_ALERT
                "member_join" -> MEMBER_JOIN
                "member_leave" -> MEMBER_LEAVE
                "*" -> ALL
                else -> UNKNOWN
            }
        }
    }
}