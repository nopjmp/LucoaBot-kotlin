package moe.giga.discord.util

enum class AccessLevel {
    USER, MOD, ADMIN, ROOT;

    override fun toString(): String {
        return when (this) {
            USER -> "user"
            MOD -> "mod"
            ADMIN -> "admin"
            ROOT -> "root"
        }
    }

    companion object {

        fun fromString(str: String): AccessLevel {
            return when (str.toLowerCase()) {
                "user" -> USER
                "mod" -> MOD
                "admin" -> ADMIN
                "root" -> ROOT
                else -> USER
            }
        }
    }

}
