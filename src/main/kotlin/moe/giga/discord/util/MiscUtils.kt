package moe.giga.discord.util

import net.dv8tion.jda.core.entities.User

object MiscUtils {
    fun username(user: User): String {
        return String.format("%s#%s", user.name, user.discriminator)
    }
}
