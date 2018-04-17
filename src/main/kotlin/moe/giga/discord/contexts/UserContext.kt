package moe.giga.discord.contexts

import moe.giga.discord.permissions.AccessLevel
import net.dv8tion.jda.core.entities.Member
import net.dv8tion.jda.core.entities.User

class UserContext(val user: User, serverContext: ServerContext) {
    private val permissions: AccessLevel = when {
        user === serverContext?.guild?.owner?.user -> AccessLevel.ADMIN
        else -> AccessLevel.USER
    }

    private val member: Member? = serverContext.getMember(user)

    val humanRole: String
        get() = this.permissions.toString()


    fun allowed(perms: AccessLevel): Boolean {
        return permissions >= perms
    }
}
