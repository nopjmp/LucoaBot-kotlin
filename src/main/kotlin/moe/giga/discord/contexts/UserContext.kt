package moe.giga.discord.contexts

import moe.giga.discord.SettingsManager
import moe.giga.discord.permissions.AccessLevel
import net.dv8tion.jda.core.entities.Member
import net.dv8tion.jda.core.entities.User

class UserContext(val user: User, serverContext: ServerContext) {
    private val permissions: AccessLevel = when {
        user.id == SettingsManager.instance.settings.ownerId -> AccessLevel.ROOT
        user === serverContext.guild.owner?.user -> AccessLevel.ADMIN
        else -> AccessLevel.USER
    }

    val member: Member? by lazy { serverContext.getMember(user) }

    val humanRole: String by lazy { this.permissions.toString() }

    val asText: String by lazy { "**%s#%s**".format(user.name, user.discriminator) }

    fun allowed(perms: AccessLevel): Boolean {
        return permissions >= perms
    }
}
