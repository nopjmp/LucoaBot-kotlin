package moe.giga.discord.contexts

import moe.giga.discord.SettingsManager
import moe.giga.discord.util.AccessLevel
import moe.giga.discord.util.username
import net.dv8tion.jda.core.entities.Member
import net.dv8tion.jda.core.entities.User

class UserContext(val user: User, serverContext: ServerContext?) {

    private val permissions: AccessLevel = when {
        user.id == SettingsManager.instance.settings.ownerId -> AccessLevel.ROOT
        serverContext == null -> AccessLevel.USER
        user === serverContext.guild.owner?.user -> AccessLevel.ADMIN
        else -> serverContext.resolvePermissions(user)
    }

    val member: Member? by lazy { serverContext?.getMember(user) }

    val humanRole: String by lazy { this.permissions.toString() }

    val asText: String by lazy { "**${user.username()}**" }

    fun allowed(perms: AccessLevel): Boolean = permissions >= perms
}
